package br.com.classes;

public class Main {

	public static void main(String[] args) {
		TFiDF test = new TFiDF();
		test.loadDirectory(args[0]);
		test.calculateTFiDF();
		test.save(args[0]+"result");
	}
}
