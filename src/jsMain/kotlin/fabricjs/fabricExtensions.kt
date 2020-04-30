package fabricjs

operator fun Point.plus(other : Point) : Point{
    return this.add(other)
}

operator fun Point.minus(other: Point): Point {
    return this.subtract(other)
}

operator fun Point.times(other: Int) : Point {
    return this.multiply(other)
}
