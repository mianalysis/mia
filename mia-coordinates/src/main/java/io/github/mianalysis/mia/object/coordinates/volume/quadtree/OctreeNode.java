package io.github.mianalysis.mia.object.coordinates.volume.quadtree;

/**
 * Created by JDJFisher on 19/07/2019.
 */
public class OctreeNode
{
    public boolean coloured;
    public OctreeNode lnw, lne, lsw, lse, unw, une, usw, use;

    public OctreeNode()
    {
        this.coloured = false;
    }

    public OctreeNode(boolean coloured)
    {
        this.coloured = coloured;
    }

    // copy constructor
    public OctreeNode(OctreeNode otNode)
    {
        if (otNode.isDivided())
        {
            lnw = new OctreeNode(otNode.lnw);
            lne = new OctreeNode(otNode.lne);
            lsw = new OctreeNode(otNode.lsw);
            lse = new OctreeNode(otNode.lse);
            unw = new OctreeNode(otNode.unw);
            une = new OctreeNode(otNode.une);
            usw = new OctreeNode(otNode.usw);
            use = new OctreeNode(otNode.use);
        }
        else
        {
            coloured = otNode.coloured;
        }
    }

    protected void subDivide()
    {
        lnw = new OctreeNode(coloured);
        lne = new OctreeNode(coloured);
        lsw = new OctreeNode(coloured);
        lse = new OctreeNode(coloured);
        unw = new OctreeNode(coloured);
        une = new OctreeNode(coloured);
        usw = new OctreeNode(coloured);
        use = new OctreeNode(coloured);
    }

    public boolean isDivided()
    {
        return lnw != null;
    }

    public boolean isLeaf()
    {
        return lnw == null;
    }

    public OctreeNode[] getChildren()
    {
        return new OctreeNode[] {lnw, lne, lsw, lse, unw, une, usw, use};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OctreeNode node = (OctreeNode) o;

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