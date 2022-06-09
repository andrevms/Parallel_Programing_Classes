package br.com.classes;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.All)
@Warmup(iterations = 60, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
public class TFiDFBenchmark {
	
	private TFiDFMutex teste;

	@Setup
	public void setup() {
		teste = new TFiDFMutex();
		teste.loadDirectorySerial("src/main/resources/arquivostxt");
	}
	
	@Benchmark
	public void calculateTFiDF(Blackhole bh) {
			teste.calculateTFiDF();
			bh.consume(teste);
	}
	

	
	
}
