# NSV Scala

Scala implementation of the [NSV (Newline-Separated Values)](https://nsv-format.org) format.

## Installation

Published to [GitHub Packages](https://github.com/nsv-format/nsv-scala/packages). Add the resolver and dependency to your `build.sbt`:

```scala
resolvers += "GitHub nsv-format" at "https://maven.pkg.github.com/nsv-format/nsv-scala"
credentials += Credentials(
  "GitHub Package Registry", "maven.pkg.github.com", "_", sys.env("GITHUB_TOKEN")
)

// Scala 3
libraryDependencies += "org.nsv-format" %% "nsv-scala" % "0.3.0"

// Scala 2.13
libraryDependencies += "org.nsv-format" %% "nsv-scala" % "0.3.0"
```

For local development:

```sh
sbt publishLocal
```

## Usage

### Basic encoding/decoding

```scala
import org.nsvformat.Nsv

// Decode from string
val data = Nsv.decode("a\nb\nc\n\nd\ne\nf\n\n")
// Seq(Seq("a", "b", "c"), Seq("d", "e", "f"))

// Encode to string
val encoded = Nsv.encode(Seq(Seq("a", "b"), Seq("c", "d")))
// "a\nb\n\nc\nd\n\n"
```

### Cell-level escaping

```scala
// Escape individual cells
Nsv.escape("hello\nworld")  // "hello\\nworld"
Nsv.escape("")              // "\\"

// Unescape
Nsv.unescape("hello\\nworld")  // "hello\nworld"
Nsv.unescape("\\")             // ""
```

### Incremental reading (streaming, tailing)

```scala
import org.nsvformat.Reader
import java.io.File

// From file
val reader = Reader.fromFile(new File("data.nsv"))
for (row <- reader) {
  println(row)  // Each row is Seq[String]
}

// From any java.io.Reader
val reader = new Reader(someJavaReader)
```

**Resumable semantics**: Reader preserves partial state across EOF, enabling tailing/streaming:

```scala
val reader = new Reader(socketInputStream)

while (true) {
  if (reader.hasNext) {
    val row = reader.next()
    processRow(row)
  } else {
    // No complete row available, wait for more data
    Thread.sleep(100)
  }
}
```

### Incremental writing

```scala
import org.nsvformat.Writer

val writer = Writer.fromFile(new File("output.nsv"))

writer.writeRow(Seq("a", "b", "c"))
writer.writeRow(Seq("d", "e", "f"))

// Or write multiple rows at once
writer.writeRows(data)
```

### Structural operations (spill/unspill)

```scala
import org.nsvformat.Util

// Flatten with terminators
val flat = Util.spill(Seq(Seq("a", "b"), Seq("c")), "")
// Seq("a", "b", "", "c", "")

// Recover structure
val structured = Util.unspill(flat, "")
// Seq(Seq("a", "b"), Seq("c"))

// Generic over types
Util.spill(Seq(Seq(1, 2), Seq(3)), -1)
// Seq(1, 2, -1, 3, -1)
```

## API

### Nsv object

- `decode(s: String): Seq[Seq[String]]` - Parse NSV string to seqseq
- `encode(data: Seq[Seq[String]]): String` - Serialize seqseq to NSV string
- `escape(s: String): String` - Escape cell content (handles `\`, `\n`, empty string)
- `unescape(s: String): String` - Unescape cell content

### Reader class

Iterator for row-by-row reading with resumable semantics:

- `new Reader(reader: java.io.Reader)` - Construct from any Reader
- `Reader.fromFile(file: java.io.File)` - Convenience factory (buffered)
- `Reader.fromPath(path: java.nio.file.Path)` - Convenience factory (buffered)
- `hasNext: Boolean` - Check if complete row available (non-terminal)
- `next(): Seq[String]` - Get next row

### Writer class

- `new Writer(writer: java.io.Writer)` - Construct from any Writer
- `Writer.fromFile(file: java.io.File)` - Convenience factory (buffered)
- `Writer.fromPath(path: java.nio.file.Path)` - Convenience factory (buffered)
- `writeRow(row: Seq[String]): Unit` - Write single row
- `writeRows(rows: Seq[Seq[String]]): Unit` - Write multiple rows

### Util object

Generic structural operations:

- `spill[T](seqseq: Seq[Seq[T]], marker: T): Seq[T]` - Flatten dimension with terminators
- `unspill[T](seq: Seq[T], marker: T): Seq[Seq[T]]` - Recover dimension by picking up terminators

## Testing

```sh
sbt test
```

63 tests covering:
- Escape/unescape operations
- Encode/decode invertibility
- Spill/unspill operations
- Reader/Writer functionality
- Resumable reading semantics
- Edge cases (empty rows, escape sequences, etc.)
