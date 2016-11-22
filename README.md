# huge-file-reverse

Reverses a huge file which doesn't feat in memory, and writes the result to target file.
Finds the 5 most popular words in the file (reversed).

Assumptions:
- Assuming the hashmap of distinct words in the file can fit in memory (otherwise counting top words would need constant reading/writing to additional file).
- Assuming file's size is less than 2^63 bytes, as this is the max value of Long.
- Assuming the sliceSize is bigger than any word in the file (otherwise word cutting may occur)
- Assuming the file is encoded in UTF-8
