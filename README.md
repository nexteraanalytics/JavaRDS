# JavaRDS

Create R-style RDS data from Java

This implements a simple writer for [R's RDS data file 
format](https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats) in a Java API.  

The API here doesn't implement GZip compression, but that could be added if necessary.  It can
also be achieved by adding a suitable compression layer to the output methods' `OutputStream` argument.

In the future, it might be desirable to write an RDS reader too, but that's not implemented yet.

I was motivated to write this code in order to support creating `archive.rds` files in the 
Sonatype Nexus R Repository Plugin (see https://github.com/sonatype-nexus-community/nexus-repository-r/issues/21),
so it only really supports enough of R's data types for that.  More types could be added in the
future if that would be useful.

# Building & releasing

In general I'm following the instructions at
http://www.albertgao.xyz/2018/01/18/how-to-publish-artifact-to-maven-central-via-gradle/ .

## One-time setup

1. I signed up for an account on https://issues.sonatype.org/, with the username `kenahoo`.

2. I applied for the `com.nexteraanalytics` namespace (https://issues.sonatype.org/browse/OSSRH-47984)
   1. As part of this ticket, we had to prove we own the `nexteraanalytics.com` domain name, which
      we did by adding a custom TXT entry to the DNS record.

3. I created a GPG key for signing releases (actually I already had one, so I just imported it), its
id is `B7EF9476`.

4. I created a `gradle.properties` file with my info:
   ```
   nexusUsername=kenahoo
   nexusPassword=MY_SONATYPE_USER_PASSWORD
   signing.keyId=B7EF9476
   signing.password=MY_GPG_KEY_PASSWORD
   ```

5. I commented out the `nexusUsername` and `nexusPassword` entries in `~/.gradle/gradle.properties`,
because annoyingly, they have higher precedence than the ones in the local file.


## Making a release

1. I executed `gradle uploadArchives`, it completed without errors.  It uploaded several (8?)
artifacts to https://oss.sonatype.org/service/local/staging/deploy/maven2/ .

2. At https://oss.sonatype.org/#stagingRepositories, I found the `comnexteraanalytics-1000`
repository and hit the "Close" button on it.

3. After closing the repo, I waited a few minutes for the "Release" button to be active, then I clicked it.

The releases can be found at e.g. https://search.maven.org/search?q=a:JavaRDS .
