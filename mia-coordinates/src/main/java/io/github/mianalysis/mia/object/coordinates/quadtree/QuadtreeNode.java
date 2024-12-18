package io.github.mianalysis.mia.object.coordinates.quadtree;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QuadtreeNode
{
    public boolean coloured;
    public QuadtreeNode nw, ne, sw, se;

    public QuadtreeNode()
    {
        this.coloured = false;
    }

    public QuadtreeNode(boolean coloured)
    {
        this.coloured = coloured;
    }

    // copy constructor
    public QuadtreeNode(QuadtreeNode qtNode)
    {
        if (qtNode.isDivided())
        {
            nw = new QuadtreeNode(qtNode.nw);
            ne = new QuadtreeNode(qtNode.ne);
            sw = new QuadtreeNode(qtNode.sw);
            se = new QuadtreeNode(qtNode.se);
        }
        else
        {
            coloured = qtNode.coloured;
        }
    }

    protected void subDivide()
    {
        nw = new QuadtreeNode(coloured);
        ne = new QuadtreeNode(coloured);
        sw = new QuadtreeNode(coloured);
        se = new QuadtreeNode(coloured);
    }

    public boolean isDivided()
    {
        return nw != null;
    }

    public boolean isLeaf()
    {
        return nw == null;
    }

    public QuadtreeNode[] getChildren()
    {
        return new QuadtreeNode[] {nw, ne, sw, se};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuadtreeNode node = (QuadtreeNode) o;

        if (isLeaf() && node.isLeaf()) return coloured == node.coloured;

        return isDivided() &&
                node.isDivided()  &&
                nw.equals(node.nw) &&
                ne.equals(node.ne) &&
                sw.equals(node.sw) &&
                se.equals(node.se);
    }

    @Override
    public int hashCode()
    {
        int result = (coloured ? 1 : 0);
        result = 31 * result + (nw != null ? nw.hashCode() : 0);
        result = 31 * result + (ne != null ? ne.hashCode() : 0);
        result = 31 * result + (sw != null ? sw.hashCode() : 0);
        result = 31 * result + (se != null ? se.hashCode() : 0);
        return result;
    }
}