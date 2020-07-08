# Bloom Filter Portable

This library provides a short implementation 
of the [Bloom Filter](https://en.wikipedia.org/wiki/Bloom_filter)  
and makes it easy to build and share the 
created bloom filter between processes. 

## Usage

The bloom filter helps to avoid transmitting (storing)
all keys of a set. With a given low probability of 
*false positive* one can say if a given key is
indeed in the set. It means the implementation may 
accept some elements that are not in the original set, 
in fact, the probability of that is quite low.

**Use case 1**: instead of query a network for easy key, 
one could download a bloom filter, only for matching
keys the request is necessary.

**Benefit** less network requests needed




## Library API

We especially separate the building of a bloom 
filter from using it. The built bloom filter
is serializable, so it can be transferred over
a network or be places on a CDN.

The API does not support updates, instead 
one may build a new bloom filter (probably of 
different optimal size). Of course building
yet another Bloom filter for a difference is
possible.  

## License
Apache 2.0

## Multiplatfom Kotlin (KMP, HMPP)

Right now the library supports JVM only, 
later versions could support other targets
too.

