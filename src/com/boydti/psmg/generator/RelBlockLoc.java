package com.boydti.psmg.generator;

public class RelBlockLoc {
    public int x;
    public int y;
    public int z;

    public RelBlockLoc(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private int hash;

    @Override
    public int hashCode() {
        if (this.hash == 0) {
            this.hash = this.x;
            this.hash = (this.hash << 4) + this.z;
            this.hash = (this.hash << 8) + this.y;
        }
        return this.hash;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj.hashCode() == hashCode();
        //    if (this == obj) {
        //      return true;
        //    }
        //    if (obj == null) {
        //      return false;
        //    }
        //    if (getClass() != obj.getClass()) {
        //      return false;
        //    }
        //    RelBlockLoc other = (RelBlockLoc) obj;
        //    return (other.x == x) && (other.z == z) && (other.y == y);
    }
}
