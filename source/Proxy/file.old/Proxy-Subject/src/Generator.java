import java.util.concurrent.ThreadLocalRandom;

public class Generator extends Thread{
	Registration R;
	CacheTable C;
	public Generator(Registration s,CacheTable c){
		R = s;
		C = c;
	}
	@Override
	public void run(){
		while(true){
			try{
				System.out.println("Preparazione nuovo valore");
				Thread.sleep(ThreadLocalRandom.current().nextInt(5000, 15000));
				double value = ThreadLocalRandom.current().nextDouble(1.0, 3.0);
				int maxage = ThreadLocalRandom.current().nextInt(10, 30);
				C.insertData(new SensorData(R,value,maxage,false));
				//
			}catch(InterruptedException ex){
				ex.printStackTrace();
			}
			
		}
	}

}