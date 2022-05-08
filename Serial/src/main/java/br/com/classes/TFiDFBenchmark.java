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
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
public class TFiDFBenchmark {
	
	private TFiDF teste;

	@Setup
	public void setup() {
		teste = new TFiDF();
		teste.loadDirectory("src/main/resources/arquivostxt");
	}
	
	@Benchmark
	public void loadFiles(Blackhole bh) {
		teste.loadDirectory("src/main/resources/arquivostxt");
		bh.consume(teste);
	}
	
	@Benchmark
	public void calculateTFiDF(Blackhole bh) {
		teste.calculateTFiDF();
		bh.consume(teste);
	}
	
	@Benchmark
	public void saveFiles(Blackhole bh) {
		teste.save("src/main/resources/result");
		bh.consume(teste);
	}
	
	
	
}
