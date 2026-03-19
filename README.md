# JavaMathLib

A lightweight, dependency-free Java mathematics library for machine learning, simulation, procedural generation, and numerically intensive applications. The library provides matrix algebra, vector mathematics, fixed-point arithmetic, trigonometry, Perlin noise, and a broad collection of numeric utility functions — all in a single JAR with no external dependencies.

**Package:** `org.antic.maths`  
**Author:** Mario Gianota  
**License:** JavaMathLib Commercial License v1.0 — free until annual revenue from commercial use exceeds USD $10,000  
**Java version:** Java 8 or later

---

## Table of Contents

- [Overview](#overview)
- [Classes at a Glance](#classes-at-a-glance)
- [Getting Started](#getting-started)
- [Matrix](#matrix)
  - [Creating a Matrix](#creating-a-matrix)
  - [Arithmetic](#arithmetic)
  - [Matrix Multiplication](#matrix-multiplication)
  - [Transpose and Inverse](#transpose-and-inverse)
  - [Element-wise Mapping](#element-wise-mapping)
  - [Utility Methods](#utility-methods)
- [Vector](#vector)
  - [Creating a Vector](#creating-a-vector)
  - [Arithmetic and Geometry](#arithmetic-and-geometry)
- [FixedArithmetic](#fixedarithmetic)
  - [How It Works](#how-it-works)
  - [Creating Values](#creating-values)
  - [Arithmetic Operations](#arithmetic-operations)
  - [Mathematical Functions](#mathematical-functions)
- [FixedTrigonometry](#fixedtrigonometry)
- [MathUtils](#mathutils)
- [Function Interface](#function-interface)
- [FixedArithmetic vs BigDecimal](#fixedarithmetic-vs-bigdecimal)
- [Building the Library](#building-the-library)
- [Generating Javadoc](#generating-javadoc)
- [Known Issues](#known-issues)
- [License](#license)

---

## Overview

JavaMathLib is structured around two complementary tiers of mathematics:

**Floating-point tier** (`Matrix`, `Vector`, `MathUtils`, `PerlinNoise`) — fast, ergonomic, and well-suited for machine learning weight matrices, procedural graphics, simulations, and game development, where `double` precision is sufficient and raw throughput matters.

**Fixed-point tier** (`FixedArithmetic`, `FixedTrigonometry`) — exact integer arithmetic with nine decimal places of fractional precision, implemented without using Java's `*`, `/`, or `%` operators anywhere in the core. Designed for embedded targets, safety-critical systems, financial calculations, and environments where floating-point behaviour must be deterministic and auditable.

Both tiers are fully documented with Javadoc and are independent of each other and of all external libraries.

---

## Classes at a Glance

| Class | Tier | Description |
|---|---|---|
| `Matrix` | Floating-point | 2D matrix with ML-oriented operations |
| `Vector` | Floating-point | 3D (and 2D) vector mathematics |
| `MathUtils` | Floating-point | Static numeric utilities: clamp, lerp, map, noise, random |
| `PerlinNoise` | Floating-point | Ken Perlin's classic 3D noise algorithm |
| `FixedArithmetic` | Fixed-point | Exact arithmetic using only integer addition and subtraction |
| `FixedTrigonometry` | Fixed-point | sin, cos, tan, asin, acos, atan, atan2 via FixedArithmetic |
| `Function` | Interface | Single-method interface for `Matrix.map()` element-wise transforms |
| `NonInvertibleMatrixException` | Exception | Thrown by `Matrix.inverse()` for non-square matrices |

---

## Getting Started

All classes are in the `org.antic.maths` package. Copy the source files into your project, or build a JAR using the supplied build script:

```bash
# Compile all sources into the classes/ directory
javac -d classes src/org/antic/maths/*.java

# Package into a JAR preserving the package hierarchy
jar cvf javamathlib.jar -C classes .
```

Or using the `build.sh` ZSH build script included with this repository:

```bash
./build.sh -s src -c classes -o javamathlib.jar
```

Add the JAR to your project's classpath and import as needed:

```java
import org.antic.maths.Matrix;
import org.antic.maths.Vector;
import org.antic.maths.FixedArithmetic;
import org.antic.maths.FixedTrigonometry;
import org.antic.maths.MathUtils;
```

---

## Matrix

The `Matrix` class provides a full-featured 2D matrix backed by a `double[][]` array. It is designed with machine learning workloads in mind but is broadly useful for any application requiring matrix computations.

### Creating a Matrix

```java
// Empty matrix — populate with readData()
Matrix a = new Matrix();

// Pre-sized, all elements zero
Matrix b = new Matrix(3, 3);

// Load from a 2D array
double[][] data = { {1, 2, 3}, {4, 5, 6} };
a.readData(data);

// Create a column-vector matrix from a 1D array
Matrix col = Matrix.fromArray(new double[]{ 1.0, 2.0, 3.0 });
```

### Arithmetic

All arithmetic returns a new `Matrix`; the original is not modified.

```java
Matrix sum      = a.add(b);          // matrix + matrix
Matrix shifted  = a.add(10.0);       // matrix + scalar
Matrix diff     = a.subtract(b);     // matrix - matrix
Matrix reduced  = a.subtract(1.0);   // matrix - scalar
Matrix scaled   = a.multiply(2.0);   // scalar multiplication
Matrix divided  = a.divide(2.0);     // scalar division
Matrix squared  = a.square();        // element-wise squaring
double total    = a.sum();           // sum of all elements
```

### Matrix Multiplication

Two distinct multiplication modes are available:

```java
// dot() — standard matrix product A × B (dimensions must be compatible)
Matrix product  = a.dot(b);

// multiply(Matrix) — element-wise (Hadamard) product (dimensions must match exactly)
Matrix hadamard = a.multiply(b);
```

> **Important:** `dot()` is the conventional matrix multiplication (rows × columns). `multiply(Matrix)` is element-wise. The naming is intentional but counter-intuitive — consult the Javadoc before use.

### Transpose and Inverse

```java
Matrix t = m.transpose();

try {
    Matrix inv = m.inverse();
} catch (NonInvertibleMatrixException e) {
    System.out.println("Cannot invert: " + e.getMessage());
}
```

> ⚠️ **Known Bug:** `inverse()` modifies the original matrix as a side effect due to in-place Gaussian elimination. See the [Known Issues](#known-issues) section for the fix.

### Element-wise Mapping

The `map()` method accepts a `Function` implementation (or a Java 8+ lambda) and applies it to every element — essential for activation functions in neural networks:

```java
// Sigmoid activation
Matrix activated = layer.map(x -> 1.0 / (1.0 + Math.exp(-x)));

// ReLU activation
Matrix relu = layer.map(x -> Math.max(0, x));

// tanh activation
Matrix tanh = layer.map(x -> Math.tanh(x));

// Static variant
Matrix result = Matrix.map(weights, x -> x * x);
```

### Utility Methods

```java
m.randomize();           // fill with random doubles in [-1, 1]
m.ones();                // fill with 1.0
m.zeros();               // fill with 0.0
m.fill(0.5);             // fill with any value

boolean sq  = m.isSquare();
int     r   = m.getRows();
int     c   = m.getColumns();
double  val = m.getValueAt(1, 2);
m.setValueAt(0, 0, 99.0);

Matrix  col  = m.getColumn(1);
double[] arr = m.toArray();         // row-major flat array

m.print();                          // print to stdout
boolean eq = a.equals(b);          // element-wise equality
Matrix  cp = m.copy();              // deep copy
```

---

## Vector

The `Vector` class models a 3D vector (with 2D convenience constructors). Instance arithmetic methods mutate and return `this`; static methods return a new `Vector`.

### Creating a Vector

```java
Vector v3   = new Vector(1.0, 2.0, 3.0);   // 3D
Vector v2   = new Vector(1.0, 2.0);         // 2D, z = 0
Vector zero = new Vector();                 // (0, 0, 0)

Vector unit = Vector.fromAngle(Math.PI / 4);  // unit vector at 45°
Vector rand = Vector.random();                // random 2D unit vector
```

### Arithmetic and Geometry

```java
// Instance methods — mutate this vector
a.add(b);       a.sub(b);       a.mult(2.0);    a.div(2.0);

// Static methods — return a new Vector
Vector sum  = Vector.add(a, b);
Vector diff = Vector.sub(a, b);

// Geometry
double mag    = v.mag();                // Euclidean length
double magSq  = v.magSq();             // squared length (faster)
Vector norm   = v.copy().normalize();  // unit vector
v.limit(5.0);                          // cap magnitude
v.setMag(3.0);                         // set to exact magnitude

// Products
double dot    = a.dot(b);              // scalar dot product
Vector cross  = a.cross(b);           // 3D cross product

// Distance and angle
double dist   = Vector.dist(a, b);    // Euclidean distance
double angle  = v.heading();          // 2D heading in radians

// Copy
Vector copy   = v.copy();             // or v.clone()
```

---

## FixedArithmetic

`FixedArithmetic` provides exact decimal arithmetic to nine fractional decimal places without using Java's `*`, `/`, or `%` operators anywhere in the arithmetic core. All computation is carried out using integer addition and subtraction only, making the class suitable for environments where floating-point non-determinism is unacceptable.

### How It Works

Every value is stored as a scaled 64-bit integer:

```
realValue = internalRegister / SCALE
SCALE = 10^9 = 1,000,000,000
```

This gives nine decimal places of fractional precision. The four arithmetic operations are implemented as follows:

| Operation | Algorithm |
|---|---|
| Add / Subtract | Direct scaled-integer addition or subtraction — a single operation |
| Multiply | Russian-peasant (binary) multiplication using only addition |
| Divide | Long division by repeated subtraction with doubling acceleration |

### Creating Values

```java
// From a long integer
FixedArithmetic a = FixedArithmetic.of(7);

// From a decimal string (parsed without floating-point or BigDecimal)
FixedArithmetic b = FixedArithmetic.of("3.14");
FixedArithmetic c = FixedArithmetic.of("-0.5");
FixedArithmetic d = FixedArithmetic.of("0.333333333");
```

### Arithmetic Operations

```java
FixedArithmetic x = FixedArithmetic.of(7);
FixedArithmetic y = FixedArithmetic.of(3);

FixedArithmetic sum  = x.add(y);       // 10.0
FixedArithmetic diff = x.subtract(y);  // 4.0
FixedArithmetic prod = x.multiply(y);  // 21.0
FixedArithmetic quot = x.divide(y);    // 2.333333333

// Operations are chainable
FixedArithmetic result = FixedArithmetic.of(7)
    .add(FixedArithmetic.of(3))
    .multiply(FixedArithmetic.of(10).subtract(FixedArithmetic.of(4)))
    .divide(FixedArithmetic.of(5));     // (7+3)*(10-4)/5 = 12.0

// Query the result
long   ip  = result.integerPart();     // 12
long   rem = result.remainder();       // 0  (in units of 10^-9)
String s   = result.toString();        // "12.0"
```

### Mathematical Functions

In addition to the four basic operations, `FixedArithmetic` provides:

```java
FixedArithmetic val = FixedArithmetic.of("-3.75");

// Absolute value
FixedArithmetic abs   = val.abs();      // 3.75

// Floor
FixedArithmetic floor = val.floor();    // -4.0

// Square root (Newton-Raphson iteration, 20 steps)
FixedArithmetic root  = FixedArithmetic.of(2).sqrt();   // 1.414213562

// Power — integer, negative, and fractional exponents all supported
FixedArithmetic pow1  = FixedArithmetic.of(2).pow(FixedArithmetic.of(10));             // 1024.0
FixedArithmetic pow2  = FixedArithmetic.of(2).pow(FixedArithmetic.of(-3));             // 0.125
FixedArithmetic pow3  = FixedArithmetic.of("2.0").pow(FixedArithmetic.of("0.5"));      // 1.414213562
FixedArithmetic pow4  = FixedArithmetic.of(27).pow(FixedArithmetic.of("0.333333333")); // ≈ 3.0
```

`sqrt()` uses Newton–Raphson (Heron's method) with a binary-search seed, entirely through `FixedArithmetic` operations.

`pow()` handles four cases: zero base, integer exponents (binary exponentiation), negative integer exponents (reciprocal), and fractional exponents via `exp(b × ln(a))` where both `exp` and `ln` are computed by Taylor series built entirely from `FixedArithmetic` operations.

---

## FixedTrigonometry

`FixedTrigonometry` provides the complete set of trigonometric functions, all computed entirely through `FixedArithmetic` operations. No `Math.*` functions, no floating-point arithmetic, and no native calls are used.

```java
FixedArithmetic angle = FixedArithmetic.of("1.5707963");  // π/2

FixedArithmetic sinVal  = FixedTrigonometry.sin(angle);   // ≈ 1.0
FixedArithmetic cosVal  = FixedTrigonometry.cos(angle);   // ≈ 0.0

FixedArithmetic asinVal = FixedTrigonometry.asin(FixedArithmetic.of("0.5"));  // ≈ 0.523598775 (π/6)
FixedArithmetic acosVal = FixedTrigonometry.acos(FixedArithmetic.of("0.5"));  // ≈ 1.047197551 (π/3)
FixedArithmetic atanVal = FixedTrigonometry.atan(FixedArithmetic.of(1));      // ≈ 0.785398163 (π/4)

FixedArithmetic a2 = FixedTrigonometry.atan2(
    FixedArithmetic.of(1), FixedArithmetic.of(1));         // ≈ 0.785398163
```

**Algorithms used:**

| Function | Algorithm |
|---|---|
| `sin` / `cos` | Taylor series with range reduction to [−π/4, π/4]; 20 terms |
| `tan` | `sin(x) / cos(x)` with near-zero cosine guard |
| `atan` | Taylor series, range-reduced to \|x\| ≤ 1 via half-angle identity |
| `asin` | `atan(x / sqrt(1 − x²))` |
| `acos` | `π/2 − asin(x)` |
| `atan2` | Quadrant-aware wrapper around `atan` |

Results agree with `java.lang.Math` to within approximately ±2 ULP at 9 decimal places.

---

## MathUtils

A collection of static utility methods covering clamping, interpolation, range mapping, random number generation, and Perlin noise. No instantiation required.

```java
// Clamping
double clamped = MathUtils.constrain(150.0, 0.0, 100.0);   // 100.0
int    clampI  = MathUtils.constrain(5, 1, 3);              // 3

// Normalise to [0, 1]
double norm    = MathUtils.normalize(75.0, 0.0, 100.0);     // 0.75

// Re-map from one range to another
double mapped  = MathUtils.map(0.5, 0.0, 1.0, 0.0, 255.0); // 127.5

// Linear interpolation
double lerped  = MathUtils.lerp(0.0, 100.0, 0.25);          // 25.0

// Step toward a goal
double current = MathUtils.approach(10.0, 0.0, 3.0);        // 3.0

// Random numbers
double rd = MathUtils.random();                              // uniform [-1.0, 1.0)
double d  = MathUtils.nextDouble(0.0, 1.0);
float  f  = (float) MathUtils.nextFloat(0f, 1f);
int    n  = MathUtils.nextInt(1, 7);

// Perlin noise — returns values in approximately [-1, 1]
double n1 = MathUtils.noise(0.5);
double n2 = MathUtils.noise(x * 0.01, y * 0.01);
double n3 = MathUtils.noise(x * 0.01, y * 0.01, t * 0.005);

// Rescale noise to pixel brightness
double pixel = MathUtils.map(MathUtils.noise(x * 0.01, y * 0.01), -1.0, 1.0, 0.0, 255.0);
```

---

## Function Interface

`Function` is a single-method (`@FunctionalInterface`-compatible) interface used by `Matrix.map()`. In Java 8 and later it is satisfied by a lambda expression:

```java
Function sigmoid = x -> 1.0 / (1.0 + Math.exp(-x));
Function relu    = x -> Math.max(0, x);
Function tanh    = x -> Math.tanh(x);

// Apply to a matrix
Matrix output = hiddenLayer.map(sigmoid);

// Named class for reuse across files
public class Sigmoid implements Function {
    @Override
    public double calculate(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
}
```

---

## FixedArithmetic vs BigDecimal

Java's `java.math.BigDecimal` is the standard library's answer to exact decimal arithmetic. For many use cases it is a reasonable choice. However, `FixedArithmetic` was designed to address requirements that `BigDecimal` does not meet well. The comparison below is direct and honest — including where `BigDecimal` is still the better option.

### Feature Comparison

| Characteristic | `FixedArithmetic` | `BigDecimal` |
|---|---|---|
| **Arithmetic operators used** | Addition and subtraction only | Multiplication, division, modulo |
| **Hardware multiply/divide dependency** | None | Yes |
| **Heap allocation per operation** | None — mutable `long` registers | One new object per result |
| **Garbage collection pressure** | Zero | High in tight loops |
| **Precision** | Fixed at 9 decimal places | Arbitrary (`MathContext`) |
| **Trigonometric functions** | Full set via `FixedTrigonometry` | None provided |
| **`sqrt()`** | Yes — Newton–Raphson via `FixedArithmetic` | Java 9+ only |
| **`pow()` with fractional exponents** | Yes — via `exp(b × ln(a))` | Not supported |
| **`RoundingMode` required for division** | No — truncates naturally | Yes |
| **API style** | Fluent, chainable | Verbose with `MathContext` |
| **Thread safety** | Not thread-safe (mutable state) | Immutable, thread-safe |
| **Suitable for no-FPU embedded targets** | Yes | No |
| **Arithmetic core auditability** | Pure Java loops, trivially reviewable | JVM / native internals |

### Why Choose `FixedArithmetic`?

**1. No multiply or divide operators in the arithmetic core**

Every value inside `FixedArithmetic` is computed using only `+` and `-` on `long` values. This matters in three real contexts:

- **Embedded targets without a hardware multiplier** — some microcontrollers and FPGAs have no hardware multiply instruction. `FixedArithmetic` runs on them correctly; `BigDecimal` does not.
- **Safety-critical or certifiable code** — avoidance of specific operators can be a hard certification requirement (e.g. MISRA, DO-178C). The arithmetic core of `FixedArithmetic` is a small number of simple loops that are straightforward to review, test, and formally verify.
- **Deterministic cross-platform reproducibility** — integer addition is fully deterministic across all JVM implementations and hardware. `BigDecimal` at high precision may delegate to platform-specific native routines, which can produce subtly different results on different architectures.

**2. Zero heap allocation in the arithmetic core**

`BigDecimal` is immutable — every `add()`, `multiply()`, and `divide()` allocates a new object on the heap. In a tight numerical loop (a neural network forward pass, a physics simulation, a navigation system running at 100 Hz) this generates substantial GC pressure that can cause latency spikes in real-time systems. `FixedArithmetic` stores its working state in `long` register fields and returns new instances only as final results, keeping all intermediate work on the stack.

**3. Trigonometry included — BigDecimal provides none**

`BigDecimal` has no trigonometric functions at all. If you need `sin`, `cos`, or `atan2` at decimal precision, your options with `BigDecimal` are to convert to `double` (abandoning exact arithmetic) or implement the series expansion yourself from scratch. `FixedTrigonometry` provides a complete, tested set of trig functions accurate to ±2 ULP at 9 decimal places, all built on `FixedArithmetic`.

**4. Fractional exponents — BigDecimal cannot do them**

`BigDecimal.pow()` accepts only non-negative integer exponents. `FixedArithmetic.pow()` accepts any `FixedArithmetic` exponent — integer, negative, or fractional — using `exp(b × ln(a))` computed entirely through Taylor series:

```java
// Not possible with BigDecimal.pow()
FixedArithmetic cubeRoot = FixedArithmetic.of(27)
    .pow(FixedArithmetic.of("0.333333333"));  // ≈ 3.0

FixedArithmetic sqrtTwo = FixedArithmetic.of(2)
    .pow(FixedArithmetic.of("0.5"));          // 1.414213562
```

**5. Simpler division**

`BigDecimal` throws `ArithmeticException` for non-terminating decimals unless a `RoundingMode` is supplied. This makes every division call verbose and error-prone:

```java
// BigDecimal — requires explicit RoundingMode
BigDecimal result = new BigDecimal("1")
    .divide(new BigDecimal("3"), 9, RoundingMode.HALF_UP);  // 0.333333333

// FixedArithmetic — truncates naturally at 9 decimal places
FixedArithmetic result = FixedArithmetic.of(1)
    .divide(FixedArithmetic.of(3));                          // 0.333333333
```

`FixedArithmetic` truncates naturally at its stated nine-decimal-place precision. No mode configuration is needed.

### When to Prefer `BigDecimal` Instead

`BigDecimal` remains the right choice when you need precision beyond nine decimal places, when you are implementing financial software that requires `RoundingMode` control mandated by accounting standards, or when immutability and thread safety are required without external synchronisation. For these use cases `BigDecimal` is the correct tool.

For everything else — embedded targets, real-time simulations, navigation and control systems, certifiable code, and any context where GC latency, determinism, or the absence of hardware multiply/divide matters — `FixedArithmetic` is the stronger choice.

---

## Building the Library

### Prerequisites

- Java 8 or later (JDK, not JRE)
- ZSH (for the included `build.sh` script)

### Compile and package

```bash
# Using the provided ZSH build script
./build.sh -s src -c classes -o javamathlib.jar

# Or manually
javac -d classes src/org/antic/maths/*.java
jar cvf javamathlib.jar -C classes .
```

### Verify JAR contents

```bash
jar tf javamathlib.jar
# org/antic/maths/Matrix.class
# org/antic/maths/Vector.class
# org/antic/maths/FixedArithmetic.class
# org/antic/maths/FixedTrigonometry.class
# org/antic/maths/MathUtils.class
# org/antic/maths/PerlinNoise.class
# org/antic/maths/Function.class
# org/antic/maths/NonInvertibleMatrixException.class
```

---

## Generating Javadoc

All public classes and methods are fully documented with Javadoc, including algorithm descriptions in `<pre>` blocks, `@param`, `@return`, `@throws`, and `@see` cross-references throughout.

```bash
javadoc \
  -d docs \
  -sourcepath src \
  -subpackages org.antic.maths \
  -windowtitle "JavaMathLib API" \
  -doctitle "JavaMathLib API Documentation" \
  -author \
  -version \
  -private
```

Open `docs/index.html` in a browser to view the generated documentation.

---

## Known Issues

### `Matrix.inverse()` mutates the original matrix

`inverse()` calls the private `gaussian()` helper, passing the raw internal `matrix` array directly. Because `gaussian()` performs Gaussian elimination **in place**, calling `inverse()` silently overwrites the contents of the original matrix. The returned inverse is mathematically correct, but the source matrix is left in a corrupted, partially-eliminated state after the call returns.

This bug is easy to miss because the return value is correct — it is only the caller's reference to the original matrix that is silently destroyed. In any iterative algorithm that calls `inverse()` repeatedly on the same matrix (such as a training loop), results will be incorrect from the second call onwards.

**Fix** — use a deep copy when calling `gaussian()`:

```java
// Inside inverse(), replace:
gaussian(matrix, index);

// With:
double[][] copy = new double[n][n];
for (int i = 0; i < n; i++)
    copy[i] = matrix[i].clone();
gaussian(copy, index);
// ... then use 'copy' in place of 'matrix'
// throughout the remainder of the inverse() method
```

---

## License

JavaMathLib is released under the **JavaMathLib Commercial License Agreement v1.0**.

- **Non-commercial use** (personal projects, academic research, open-source software, education) — **free, no restrictions.**
- **Commercial use where annual gross revenue attributable to this library is below USD $10,000** — **free, no fee payable.**
- **Commercial use above the USD $10,000 revenue threshold** — a tiered annual license fee applies. See `LICENSE` for the full schedule and payment terms.

Upon crossing the revenue threshold, licensees are required to notify the Licensor within 30 days and commence fee payment. No retroactive fees are owed for the pre-threshold period.

**Contact:** mariogianota@protonmail.com  
**Copyright © Mario Gianota. All rights reserved.**
