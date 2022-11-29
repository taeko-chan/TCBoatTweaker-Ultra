package ch.taeko.TCBoatTweakerUltra.Engine;

public class Engine {
    int maxRpm;
    double internalDrag;
    int rpm;
    int power;

    public Engine(int maxRpm, double internalDrag) {
	   this.maxRpm = maxRpm;
	   this.internalDrag = internalDrag;
    }

    public float getTorque() {
	   return (float) (-(210/Math.pow(3500, 4)) * Math.pow(this.rpm-3500, 4) + 210);
    }

    public void start() {
	   this.rpm = 800;
    }

    public void stop() {
	   this.rpm = 0;
    }

    public void throttle(int multiplier, int altitude) {

	   if (this.rpm < maxRpm) {
		  this.rpm += 100 * multiplier;
	   } else {
		  this.rpm -= 100;
	   }

    }

    public void run() {
	   if (this.rpm >= 800) this.rpm -= 325;
    }

    public int getRpm() {return this.rpm;}
}
