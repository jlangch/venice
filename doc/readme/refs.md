# Mutable reference types


Venice has three major mutable reference types with different use cases and APIs:

**1. Agent**

Uncoordinated, asynchronous: used for example for controlling I/O when features of the other reference types are not needed.

**2. Atom**

Uncoordinated, synchronous: for situations where a single value is required that can be read and swapped with another value.

**3. Var**

Provides global and thread-local state


_Note:_
Venice does not support Clojure's *Ref*s that provide coordinated, synchronous, and retriable access to multiple refs.