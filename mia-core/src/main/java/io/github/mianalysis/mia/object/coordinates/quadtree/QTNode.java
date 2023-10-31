package io.github.mianalysis.mia.object.coordinates.quadtree;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QTNode
{
    public boolean coloured;
    public QTNode nw, ne, sw, se;

    public QTNode()
    {
        this.coloured = false;
    }

    public QTNode(boolean coloured)
    {
        this.coloured = coloured;
    }

    // copy constructor
    public QTNode(QTNode qtNode)
    {
        if (qtNode.isDivided())
        {
            nw = new QTNode(qtNode.nw);
            ne = new QTNode(qtNode.ne);
            sw = new QTNode(qtNode.sw);
            se = new QTNode(qtNode.se);
        }
        else
        {
            coloured = qtNode.coloured;
        }
    }

    protected void subDivide()
    {
        nw = new QTNode(coloured);
        ne = new QTNode(coloured);
        sw = new QTNode(coloured);
        se = new QTNode(coloured);
    }

    public boolean isDivided()
    {
        return nw != null;
    }

    public boolean isLeaf()
    {
        return nw == null;
    }

    public QTNode[] getChildren()
    {
        return new QTNode[] {nw, ne, sw, se};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QTNode node = (QTNode) o;

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