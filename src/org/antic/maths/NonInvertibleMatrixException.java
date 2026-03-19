package org.antic.maths;

/**
 * Indicates the condition that a <code>Matrix</code> is non invertible.
 *
 * @author Mario Gianota
 */
public class NonInvertibleMatrixException extends Exception {

    public NonInvertibleMatrixException(String message) {
        super(message);
    }
}
