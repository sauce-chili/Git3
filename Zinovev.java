public class Bomb {

    private int _radius = 1;

    public int getRadius() {
        return _radius;
    }

    public void setRadius(int r) {
        _radius = r;
    }

    public void doubleRadius() {
        _radius *= 2;
    }

    public int bombardiroCrocodilo() {
        int bombardiroGusini = _radius;
        return bombardiroGusini * 3;
    }
}
