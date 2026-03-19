package org.antic.maths;
import java.util.Random;

/**
 * A bunch of static methods that perform useful mathematical operations.
 * 
 * @author Mario Gianota 
 *
 */
public class MathUtils {
	final static Random rnd = new Random();
	final static double EPSILON = 1e-12;

	public MathUtils() { }
	/**
	 * Returns true if the given value lies within the given range.
	 * 
	 * @param value the value to test
	 * @param start the start range
	 * @param end the end range
	 * @return in range
	 */
	public boolean inRange(double value, double start, double end) {
		if( value >= start && value <= end)
			return true;
		return false;
	}

	/**
	 * Constrain a number between a lower bound and upper bound such that if <em>n</n>
	 * is less than <em>lower</em> then <em>lower</em> is returned or if <em>n</em> is
	 * greater than <em>upper</em> then <em>upper</em> is returned.
	 * 
	 * @param n the number to constrain.
	 * @param lower the lower bound
	 * @param upper the upper bound
	 * @return the number constrained
	 */
	public static double constrain(double n, double lower, double upper) {
		double num = n;
		if( n < lower )
			num = lower;
		else if( n > upper )
			num = upper;
		return num;
	}
	/**
	 * Constrain an integer number between a lower bound and upper bound such that if <em>n</n>
	 * is less than <em>lower</em> then <em>lower</em> is returned or if <em>n</em> is
	 * greater than <em>upper</em> then <em>upper</em> is returned.
	 * 
	 * @param n the number to constrain.
	 * @param lower the lower bound
	 * @param upper the upper bound
	 * @return the number constrained
	 */
	public static int constrain(int n, int lower, int upper) {
		int num = n;
		if( n < lower )
			num = lower;
		else if( n > upper )
			num = upper;
		return num;
	}
	/**
	 * Normalize a value to betwwen 0 and 1 for a given range.
	 */
	public static double normalize(double value, double min, double max) {
		return (value - min) / ( max - min);
	}
	/**
	 * Approach. Returns a value approaching the goal value based on current value
	 * and an offset.
	 * 
	 * @param goal the goal value
	 * @param current the current value
	 * @param deltaT Amount to add or subtract to current value to approach goal
	 */
	public static double approach(double goal, double current, double deltaT) {
		double difference = goal - current;
		if( difference > deltaT) {
			return current + deltaT;
		}
		if( difference < -deltaT) {
			return current - deltaT;
		}
		return goal;
	}

	/**
	 * Linear interpolation, also known as "Lerp", or "Mix". The method interpolates 
	 * within the range [start..end] based on a 't' parameter, where 't' is typically 
	 * within a [0..1] range.
	 * Examples:
	 * lerp(0, 100, 0.5); // 50
	 * lerp(20, 80, 0);   // 20
	 * 
	 * @param start the starting value
	 * @param end the ending value
	 * @param t percentage value in range 0..1 
	 */
	public static double lerp(double start, double end, double t) {
		// Precise method, which guarantees v = v1 when t = 1.
		  return (1 - t) * start + t * end;
	}
	/**
	 * Generate a random double between the supplied  ranges.
	 * 
	 * @param rangeMin start range
	 * @param rangeMax end range
	 * @return the random number
	 */
	public static double nextDouble(double rangeMin, double rangeMax) {
		return rangeMin + (rangeMax - rangeMin) * rnd.nextDouble();
	}

	/**
	 * Generate a random float between the supplied  ranges.
	 * 
	 * @param rangeMin start range
	 * @param rangeMax end range
	 * @return the random number
	 */
	public static double nextFloat(float rangeMin, float rangeMax) {
		return rangeMin + (rangeMax - rangeMin) * rnd.nextFloat();
	}
	
	/**
	 * Generate a random integer between the supplied  ranges.
	 * 
	 * @param rangeMin start range
	 * @param rangeMax end range
	 * @return the random number
	 */
	public static int nextInt(int rangeMin, int rangeMax) {
		return (int) (rangeMin + (rangeMax - rangeMin) * rnd.nextDouble());
	}
	/**
	 * Returns a random {@code double} value uniformly distributed in the range
	 * {@code [-1.0, 1.0)}.
	 *
	 * <p>The value is produced by taking a sample from
	 * {@link java.util.Random#nextDouble()}, which yields a value in
	 * {@code [0.0, 1.0)}, scaling it to the interval {@code [0.0, 2.0)},
	 * and then shifting it down by {@code 1.0} to centre the range on zero:
	 *
	 * <pre>
	 *   result = (rnd.nextDouble() * 2.0) - 1.0
	 * </pre>
	 *
	 * <p>The distribution is uniform — every value within the range is equally
	 * likely. The lower bound {@code -1.0} is inclusive and the upper bound
	 * {@code +1.0} is exclusive, consistent with the contract of
	 * {@link java.util.Random#nextDouble()}.
	 *
	 * <p>Typical use cases include:
	 * <ul>
	 *   <li><b>Neural network weight initialisation.</b> Weights are commonly
	 *       initialised to small random values centred around zero so that
	 *       gradient-based training algorithms (e.g. backpropagation) can
	 *       converge correctly. Symmetric initialisation around zero prevents
	 *       neurons from learning identical features (the "symmetry breaking"
	 *       requirement).</li>
	 *   <li><b>Simulations and procedural generation.</b> When a random
	 *       perturbation, direction, or offset that may be either positive or
	 *       negative is required, this method provides a zero-centred source
	 *       of randomness without additional arithmetic.</li>
	 *   <li><b>Noise and bias generation.</b> Generating random bias terms or
	 *       additive noise that is symmetrically distributed around zero.</li>
	 * </ul>
	 *
	 * <p>Example usage:
	 * <pre>
	 *   // Initialise a matrix of neural network weights
	 *   for (int i = 0; i &lt; rows; i++)
	 *       for (int j = 0; j &lt; cols; j++)
	 *           weights[i][j] = MathUtils.random(); // e.g. -0.482, 0.763, -0.031
	 * </pre>
	 *
	 * <p>This method uses the shared {@link java.util.Random} instance held
	 * by {@code MathUtils} and is therefore not thread-safe. If concurrent
	 * access is required, callers should use
	 * {@link java.util.concurrent.ThreadLocalRandom} directly.
	 *
	 * @return a pseudorandom {@code double} in the range {@code [-1.0, 1.0)}
	 * @see #nextDouble(double, double)
	 * @see java.util.Random#nextDouble()
	 */
	public static double random() {
		return (rnd.nextDouble() * 2.0) - 1.0;
	}

	
	/**
	 * Re-maps a number from one range to another. 
	 * 
	 * @param valueCoord1 The value to be converted
	 * @param startCoord1 Lower bound of the value's current range
	 * @param endCoord1 Upper bound of the value's current range
	 * @param startCoord2 Lower bound of the value's target range
	 * @param endCoord2 Upper bound of the value's target range
	 * @return The number re-mapped
	 */
	public static double map(double valueCoord1,
	        double startCoord1, double endCoord1,
	        double startCoord2, double endCoord2) {

	    if (Math.abs(endCoord1 - startCoord1) < EPSILON) {
	        throw new ArithmeticException("/ 0");
	    }

	    double offset = startCoord2;
	    double ratio = (endCoord2 - startCoord2) / (endCoord1 - startCoord1);
	    return ratio * (valueCoord1 - startCoord1) + offset;
	}
}
