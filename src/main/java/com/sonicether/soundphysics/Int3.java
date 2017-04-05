package com.sonicether.soundphysics;

public class Int3 
{
	public int x;
	public int y;
	public int z;
	
	public static Int3 create(int x, int y, int z)
	{
		Int3 i = new Int3();
		
		i.x = x;
		i.y = y;
		i.z = z;
		
		return i;
	}
	
	@Override public boolean equals(Object b)
	{
		Int3 i = (Int3)b;
		return (this.x == i.x && this.y == i.y && this.z == i.z);
	}
	
	Int3()
	{
		
	}
}
