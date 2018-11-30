#!/usr/bin/perl

use strict;
use warnings;

# See https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats for the format definition

sub vers {
  my ($v, $p, $s) = @_;
  ($v<<16) + ($p<<8) + $s;
}

{
  package integer;
  sub new {my $c = shift(); bless shift(), $c}
  sub pack {
    my $self = shift;
    pack "N/N*", @$self;
  }
}

{
  package date;
  sub new {my $c = shift(); bless shift(), $c}
  sub pack {
    my $self = shift;
    pack "N/N*", @$self;
  }
}

{
  package dataframe;
  sub new {my ($c, $d, $names) = @_; bless[$d, $names], $c}
  sub pack {
    # TODO
  }
}

{
  package float;
  sub new {my $c = shift(); bless shift(), $c}
  sub pack {
    my $self = shift;
    pack "N/d>*", @$self;
  }
}

{
  package boolean;
  sub new {my $c = shift(); bless shift(), $c}
  sub pack {
    my $self = shift;
    pack "N/l>*", map {!!$_} @$self;
  }
}

{
  package object;
  sub new {my ($c, $s, $r) = @_; bless [$s, $r], $c}
  sub pack {
    # TODO
  }
}

{
  package string;
  sub new {my $c = shift(); bless shift(), $c}
  sub pack {
    my $self = shift;
    # The magic (32<<16) + 9 is probably an encoding marker?
    pack "N (N N/a)*", 0+@$self, map {(32<<16) + 9, $_} @$self;
  }
}

sub write_rds {
  my ($df, $file) = @_;

  open my $fh, ">", $file or die "$file: $!";
  # print $fh "X\n";  # Magic "number"
  # print $fh pack("N3",
  #   2, # Version of RDS spec
  #   vers(3, 5, 1),  # version of R which wrote the file
  #   vers(2, 3, 0),  # minimal version of R needed to read the format
  # );

  # print $fh bless([45252, 45907], 'float')->pack();
  # print $fh bless([1, 0], 'boolean')->pack();
  # print $fh string->new(["class", "classes"])->pack();
  print $fh integer->new([1001, 1010])->pack;
}

sub serialize {

}


my %data = (
  A3 => new dataframe({
    size => new float([45252, 45907]),
    isdir => new boolean([0, 0]),
    mode => new object([436, 436], ['octmode']),
    mtime => new date([1360227629.28697, 1364324320]),
    ctime => new date([1543489579.69805, 1543489579.69805]),
    atime => new date([1543450562.88206, 1543450563.03406]),
    uid => new integer([1001, 1010]),
    gid => new integer([1001, 1001]),
    uname => new string(["hornik", "ligges"]),
    grname => new string(["cranadmin", "cranadmin"]),
  }, ["A3/A3_0.9.1.tar.gz", "A3/A3_0.9.2.tar.gz"]),
);

write_rds(\%data, "dat.rds");
