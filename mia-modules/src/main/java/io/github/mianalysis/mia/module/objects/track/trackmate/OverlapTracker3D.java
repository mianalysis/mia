/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2010 - 2024 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package io.github.mianalysis.mia.module.objects.track.trackmate;

import static io.github.mianalysis.mia.module.objects.track.trackmate.OverlapTracker3DFactory.BASE_ERROR_MESSAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.scijava.Cancelable;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.util.Threads;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
import net.imglib2.algorithm.MultiThreadedBenchmarkAlgorithm;

public class OverlapTracker3D extends MultiThreadedBenchmarkAlgorithm implements SpotTracker, Cancelable {

	private SimpleWeightedGraph<Spot, DefaultWeightedEdge> graph;

	private Logger logger = Logger.VOID_LOGGER;

	private final SpotCollection spots;

	private final ObjsI objs;

	private final double minIoU;

	private final boolean allowTrackSplitting;

	private final boolean allowTrackMerging;

	private boolean isCanceled;

	private String cancelReason;

	/*
	 * CONSTRUCTOR
	 */

	public OverlapTracker3D(final SpotCollection spots, final ObjsI objs, final double minIoU,
			final boolean allowTrackSplitting, final boolean allowTrackMerging) {
		this.spots = spots;
		this.objs = objs;
		this.minIoU = minIoU;
		this.allowTrackSplitting = allowTrackSplitting;
		this.allowTrackMerging = allowTrackMerging;
	}

	/*
	 * METHODS
	 */

	@Override
	public SimpleWeightedGraph<Spot, DefaultWeightedEdge> getResult() {
		return graph;
	}

	@Override
	public boolean checkInput() {
		return true;
	}

	@Override
	public boolean process() {
		isCanceled = false;
		cancelReason = null;

		/*
		 * Check input now.
		 */

		// Check that the objects list itself isn't null
		if (null == spots) {
			errorMessage = BASE_ERROR_MESSAGE + "The spot collection is null.";
			return false;
		}

		// Check that the objects list contains inner collections.
		if (spots.keySet().isEmpty()) {
			errorMessage = BASE_ERROR_MESSAGE + "The spot collection is empty.";
			return false;
		}

		// Check that at least one inner collection contains an object.
		boolean empty = true;
		for (final int frame : spots.keySet()) {
			if (spots.getNSpots(frame, true) > 0) {
				empty = false;
				break;
			}
		}
		if (empty) {
			errorMessage = BASE_ERROR_MESSAGE + "The spot collection is empty.";
			return false;
		}

		/*
		 * Process.
		 */

		final long start = System.currentTimeMillis();

		// Instantiate graph
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// Flag if we are doing ok.
		final AtomicBoolean ok = new AtomicBoolean(true);

		// Prepare frame pairs in order, not necessarily separated by 1.
		final Iterator<Integer> frameIterator = spots.keySet().iterator();

		// First frame.
		final int sourceFrame = frameIterator.next();
		Map<Spot, Volume> sourceGeometries = createGeometry(spots.iterable(sourceFrame, true), objs);

		logger.setStatus("Frame to frame linking...");
		int progress = 0;
		int count = 0;
		while (frameIterator.hasNext()) {
			if (!ok.get() || isCanceled())
				break;

			final int targetFrame = frameIterator.next();

			final Map<Spot, Volume> targetGeometries = createGeometry(spots.iterable(targetFrame, true), objs);

			if (sourceGeometries.isEmpty() || targetGeometries.isEmpty())
				continue;

			final ExecutorService executors = Threads.newFixedThreadPool(numThreads);
			final List<Future<ArrayList<IoULink>>> futures = new ArrayList<>();

			// Submit work.
			for (final Spot target : targetGeometries.keySet()) {
				final Volume targetVolume = targetGeometries.get(target);
				futures.add(executors
						.submit(new FindBestSourcesTask(target, targetVolume, sourceGeometries, minIoU, allowTrackMerging)));
			}

			// Get results.
			HashMap<Spot, IoULink> bestSourceIoUs = new HashMap<>();
			for (final Future<ArrayList<IoULink>> future : futures) {
				if (!ok.get() || isCanceled())
					break;

				try {
					final ArrayList<IoULink> links = future.get();

					for (IoULink link : links) {
						if (link.source == null)
							continue;

						if (!allowTrackSplitting && bestSourceIoUs.containsKey(link.source))
							if (bestSourceIoUs.get(link.source).iou > link.iou)
								continue;
							else
								removeLink(bestSourceIoUs.get(link.source));
						
						addLink(link);
						bestSourceIoUs.put(link.source, link);
						link.target.putFeature("IoU", link.iou);
						
					}

				} catch (InterruptedException | ExecutionException e) {
					errorMessage = e.getMessage();
					ok.set(false);
				}
			}
			executors.shutdown();

			sourceGeometries = targetGeometries;
			logger.setProgress((double) progress++ / spots.keySet().size());

			Module.writeProgressStatus(++count,spots.keySet().size(),"frames","Overlap tracker 3D");

		}

		logger.setProgress(1d);
		logger.setStatus("");

		final long end = System.currentTimeMillis();
		processingTime = end - start;

		return ok.get();
	}

	public void addLink(IoULink link) {
		graph.addVertex(link.source);
		graph.addVertex(link.target);
		final DefaultWeightedEdge edge = graph.addEdge(link.source, link.target);
		graph.setEdgeWeight(edge, 1. - link.iou);
	}

	public void removeLink(IoULink link) {
		graph.removeEdge(link.source, link.target);
	}

	@Override
	public void setLogger(final Logger logger) {
		this.logger = logger;
	}

	protected boolean checkSettingsValidity(final Map<String, Object> settings, final StringBuilder str) {
		if (null == settings) {
			str.append("Settings map is null.\n");
			return false;
		}

		final boolean ok = true;
		return ok;

	}

	private static Map<Spot, Volume> createGeometry(final Iterable<Spot> spots, final ObjsI objs) {
		final Map<Spot, Volume> geometries = new HashMap<>();

		for (final Spot spot : spots)
			geometries.put(spot, objs.get(spot.getFeature("MIA_ID").intValue()));

		return Collections.unmodifiableMap(geometries);

	}

	private static final class FindBestSourcesTask implements Callable<ArrayList<IoULink>> {

		private final Spot target;

		private final Volume targetVolume;

		private final Map<Spot, Volume> sourceGeometries;

		private final double minIoU;

		private final boolean allowTrackMerging;

		public FindBestSourcesTask(final Spot target, final Volume targetVolume,
				final Map<Spot, Volume> sourceGeometries, final double minIoU,
				final boolean allowTrackMerging) {
			this.target = target;
			this.targetVolume = targetVolume;
			this.sourceGeometries = sourceGeometries;
			this.minIoU = minIoU;
			this.allowTrackMerging = allowTrackMerging;

		}

		@Override
		public ArrayList<IoULink> call() throws Exception {
			ArrayList<IoULink> links = new ArrayList<>();

			if (allowTrackMerging) {
				for (final Spot spot : sourceGeometries.keySet()) {
					final Volume sourceVolume = sourceGeometries.get(spot);
					final double intersection = targetVolume.getOverlap(sourceVolume);
					if (intersection == 0.)
						continue;

					final double union = sourceVolume.size() + targetVolume.size() - intersection;
					final double iou = intersection / union;
					if (iou >= minIoU)
						links.add(new IoULink(spot, target, iou));
				}
			} else {
				double maxIoU = minIoU;
				Spot bestSpot = null;
				for (final Spot spot : sourceGeometries.keySet()) {
					final Volume sourceVolume = sourceGeometries.get(spot);
					final double intersection = targetVolume.getOverlap(sourceVolume);
					if (intersection == 0.)
						continue;

					final double union = sourceVolume.size() + targetVolume.size() - intersection;
					final double iou = intersection / union;
					if (iou > maxIoU) {
						maxIoU = iou;
						bestSpot = spot;
					}
				}

				links.add(new IoULink(bestSpot, target, maxIoU));

			}

			return links;

		}
	}

	private static final class IoULink {
		public final Spot source;

		public final Spot target;

		public final double iou;

		public IoULink(final Spot source, final Spot target, final double iou) {
			this.source = source;
			this.target = target;
			this.iou = iou;
		}
	}

	// --- org.scijava.Cancelable methods ---

	@Override
	public boolean isCanceled() {
		return isCanceled;
	}

	@Override
	public void cancel(final String reason) {
		isCanceled = true;
		cancelReason = reason;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}
}