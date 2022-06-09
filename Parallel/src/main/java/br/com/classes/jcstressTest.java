package br.com.classes;

import org.openjdk.jcstress.annotations.*;

public class jcstressTest {

	@State
	public static class MyState extends TFiDFMutex{}
	
	@JCStressTest
	@Description("")
	//@Outcome()
	public static class teste1{
		//@Actor
		
	}
}
