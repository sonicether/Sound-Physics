package com.sonicether.soundphysics;

public class Int3 {
	public int x;
	public int y;
	public int z;

	public static Int3 create(final int x, final int y, final int z) {
		final Int3 i = new Int3();

		i.x = x;
		i.y = y;
		i.z = z;

		return i;
	}

	@Override
	public boolean equals(final Object b) {
		final Int3 i = (Int3) b;
		return (this.x == i.x && this.y == i.y && this.z == i.z);
	}

	Int3() {

	}
}
