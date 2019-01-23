# JavaRDS

## Create RDS data from Java

This implements a simple writer for [R's RDS data file 
format](https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats) in a Java API.  

The API here doesn't implement GZip compression, but that could be added if necessary.  It can
also be achieved by adding a suitable compression layer to the output methods' `OutputStream` argument.

In the future, it might be desirable to write an RDS reader too, but that's not implemented yet.

I was motivated to write this code in order to support creating `archive.rds` files in the 
Sonatype Nexus R Repository Plugin (see https://github.com/sonatype-nexus-community/nexus-repository-r/issues/21),
so it only really supports enough of R's data types for that.  More types could be added in the
future if that would be useful.
