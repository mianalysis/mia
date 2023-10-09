package io.github.mianalysis.mia.object.coordinates.quadtree;

/**
 * Created by JDJFisher on 19/07/2019.
 */
public class OTNode
{
    public boolean coloured;
    public OTNode lnw, lne, lsw, lse, unw, une, usw, use;

    public OTNode()
    {
        this.coloured = false;
    }

    public OTNode(boolean coloured)
    {
        this.coloured = coloured;
    }

    // copy constructor
    public OTNode(OTNode otNode)
    {
        if (otNode.isDivided())
        {
            lnw = new OTNode(otNode.lnw);
            lne = new OTNode(otNode.lne);
            lsw = new OTNode(otNode.lsw);
            lse = new OTNode(otNode.lse);
            unw = new OTNode(otNode.unw);
            une = new OTNode(otNode.une);
            usw = new OTNode(otNode.usw);
            use = new OTNode(otNode.use);
        }
        else
        {
            coloured = otNode.coloured;
        }
    }

    protected void subDivide()
    {
        lnw = new OTNode(coloured);
        lne = new OTNode(coloured);
        lsw = new OTNode(coloured);
        lse = new OTNode(coloured);
        unw = new OTNode(coloured);
        une = new OTNode(coloured);
        usw = new OTNode(coloured);
        use = new OTNode(coloured);
    }

    public boolean isDivided()
    {
        return lnw != null;
    }

    public boolean isLeaf()
    {
        return lnw == null;
    }

    public OTNode[] getChildren()
    {
        return new OTNode[] {lnw, lne, lsw, lse, unw, une, usw, use};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OTNode node = (OTNode) o;

        if (isLeaf() && node.isLeaf()) return coloured == node.coloured;

        return  isDivided() &&
                node.isDivided()  &&
                lnw.equals(node.lnw) &&
                lne.equals(node.lne) &&
                lsw.equals(node.lsw) &&
                lse.equals(node.lse) &&
                unw.equals(node.unw) &&
                une.equals(node.une) &&
                usw.equals(node.usw) &&
                use.equals(node.use);
    }

    @Override
    public int hashCode()
    {
        int result = (coloured ? 1 : 0);
        result = 31 * result + (lnw != null ? lnw.hashCode() : 0);
        result = 31 * result + (lne != null ? lne.hashCode() : 0);
        result = 31 * result + (lsw != null ? lsw.hashCode() : 0);
        result = 31 * result + (lse != null ? lse.hashCode() : 0);
        result = 31 * result + (unw != null ? unw.hashCode() : 0);
        result = 31 * result + (une != null ? une.hashCode() : 0);
        result = 31 * result + (usw != null ? usw.hashCode() : 0);
        result = 31 * result + (use != null ? use.hashCode() : 0);
        return result;
    }
}