# Hexdump


To hexdump the full contents of a file to `*out*`:

```clojure
(do
  (load-module :hexdump)
  (hexdump/hexdump "/path/to/my-file"))
```

or use an `io/file`

```clojure
(do
  (load-module :hexdump)
  (hexdump/hexdump (io/file "/path/to/my-file")))
```

You can also hexdump a collection of values directly:

```clojure
(do
  (load-module :hexdump)
  (hexdump/hexdump [104 101 108 108 111 32 115 116 114 97 110 103 101 114]))
```

You can limit the data to be hexdumped via named arguments:

```clojure
(do
  (load-module :hexdump) 
  (hexdump/hexdump "/path/to/my-file" :offset 64 :size 128))
```

## Sample Output

The following code:

```clojure
(do
  (load-module :hexdump) 
  (hexdump/hexdump (range 196))
```

prints the following to `*out*`:

```text
00000000: 0001 0203 0405 0607 0809 0a0b 0c0d 0e0f  ................
00000010: 1011 1213 1415 1617 1819 1a1b 1c1d 1e1f  ................
00000020: 2021 2223 2425 2627 2829 2a2b 2c2d 2e2f   !"#$%&'()*+,-./
00000030: 3031 3233 3435 3637 3839 3a3b 3c3d 3e3f  0123456789:;<=>?
00000040: 4041 4243 4445 4647 4849 4a4b 4c4d 4e4f  @ABCDEFGHIJKLMNO
00000050: 5051 5253 5455 5657 5859 5a5b 5c5d 5e5f  PQRSTUVWXYZ[\]^_
00000060: 6061 6263 6465 6667 6869 6a6b 6c6d 6e6f  `abcdefghijklmno
00000070: 7071 7273 7475 7677 7879 7a7b 7c7d 7e7f  pqrstuvwxyz{|}~.
00000080: 8081 8283 8485 8687 8889 8a8b 8c8d 8e8f  ................
00000090: 9091 9293 9495 9697 9899 9a9b 9c9d 9e9f  ................
000000a0: a0a1 a2a3 a4a5 a6a7 a8a9 aaab acad aeaf  ................
000000b0: b0b1 b2b3 b4b5 b6b7 b8b9 babb bcbd bebf  ................
000000c0: c0c1 c2c3                                ....            
```

