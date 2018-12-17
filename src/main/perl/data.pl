#!/usr/bin/perl

use strict;

# See https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats for the format definition





my $STRING_START = 01000011; # Probably an encoding marker?
my $NA_INT = 1<<31;


# Shortcuts
sub string { string->new(@_) }
sub pairlist { pairlist->new(@_) }
sub float { 'float'->new(@_) }
sub list { list->new(@_) }
sub null { null->new(@_) }
sub integer { integer->new(@_) }
sub boolean { boolean->new(@_) }
sub R_thing { R_thing->new(@_) }

sub date {
  float(shift())->set_class("POSIXct", "POSIXt");
}

sub named_list {
  my (@keys, @vals);
  while (@_) {
    push @keys, shift;
    push @vals, shift;
  }
  list(\@vals, pairlist([names => string(\@keys)]));
}

sub dataframe {
  my $df = named_list(@_);
  $df->set_class("data.frame")->set_attr('row.names' => integer([$NA_INT, -1]));
}

#####################################

{
  package RThing;
  sub new {
    my ($class, @d) = @_;
    bless \@d, $class;
  }

  sub flags {
    my $self = shift;
    my $flags = $self->type or return '';
    $flags |= 1<<9 if @$self > 1;
    $flags |= 1<<8 if @$self > 1 && $self->[1]->has_key("class");
    pack "N", $flags;
  }

  sub attrs {
    my $self = shift;
    $self->[1] ? $self->[1]->pack : ''
  }

  sub pack {
    my $self = shift;
    $self->flags . $self->data($self->[0]) . $self->attrs;
  }

  sub set_attr {
    my ($self, $key, $val) = @_;
    $self->[1] ||= ::pairlist([]);
    $self->[1]->set($key, $val);
    return $self;
  }

  sub set_class {
    my $self = shift;
    $self->set_attr(class => ::string([ @_ ]));
  }

  sub data {die "Unimplemented data"}
  sub type {die "Unimplemented type"}
}

{
  package integer;
  our @ISA = qw(RThing);
  sub type {015}
  sub data {
    my (undef, $d) = @_;
    pack "N/N*", @$d;
  }
}

{
  package list;
  our @ISA = qw(RThing);
  sub type {0x13}
  sub data {
    my (undef, $d) = @_;
    pack("N", 0 + @$d) . join('', map $_->pack, @$d);
  }
}

{
  package float;
  our @ISA = qw(RThing);
  sub type {016}
  sub data {
    my (undef, $d) = @_;
    pack "N/d>*", @$d;
  }
}

{
  package boolean;
  our @ISA = qw(RThing);
  sub type {012}
  sub data {
    my (undef, $d) = @_;
    pack "N/l>*", map {!!$_} @$d;
  }
}

{
  package string;
  our @ISA = qw(RThing);
  sub type {020}
  sub data {
    my (undef, $d) = @_;
    pack "N (N N/a)*", 0 + @$d, map {$STRING_START, $_} @$d;
  }
}

{
  # This is used for attributes too
  package pairlist;
  our @ISA = qw(RThing);
  sub type {}
  sub data {
    my (undef, $d) = @_;
    my $out = '';
    for (my $i = 0; $i < @$d; $i += 2) {
      $out .= pack("N", 02002);
      $out .= pack("N N N/a*", 1, $STRING_START, $d->[$i]); # name
      $out .= $d->[$i + 1]->pack;                           # value
    }
    $out .= ::null()->pack;
    $out;
  }
  sub has_key {
    my ($self, $key) = @_;
    my $d = $self->[0];
    for (my $i = 0; $i < @$d; $i += 2) {
      return 1 if $d->[$i] eq $key;
    }
    return 0;
  }
  sub set {
    my ($self, $key, $val) = @_;
    my $d = $self->[0];
    for (my $i = 0; $i < @$d; $i += 2) {
      if ($d->[$i] eq $key) {
        $d->[$i+1] = $val;
        return;
      }
    }
    push @$d, $key, $val;
  }
}

{
  package null;
  our @ISA = qw(RThing);
  sub type {0376}
  sub data {''}
}

sub vers {
  my ($v, $p, $s) = @_;
  ($v<<16) | ($p<<8) | $s;
}


sub write_rds {
  my ($df, $file) = @_;

  open my $fh, ">", $file or die "$file: $!";

  print $fh "X\n";  # Magic "number"
  print $fh pack("N3",
    2,              # Version of RDS spec
    vers(3, 5, 1),  # Version of R which wrote the file
    vers(2, 3, 0),  # Minimal version of R needed to read the format
  );

  print $fh $df->pack;

  # print $fh integer([5])->pack();
  # print $fh bless([1, 0], 'boolean')->pack();
  # print $fh string->new(["bar"])->pack();
  # print $fh float->new([9])->pack;
  # print $fh list([integer([5]), integer([6])], pairlist([names => string(['a', 'b'])]))->pack;
  # print $fh named_list(a => integer([5]), b => integer([6]))->add_attr(foo => integer([5]))->pack;
  # print $fh pairlist([food => string(["barf"]), x => string(["y"])])->pack;
  # print $fh null()->pack;
  # print $fh date([1543489579.69805, 1543489579.69805])->pack;
  # print $fh dataframe(a => integer([5]), b => string(['foo']))->pack;
}

######################################




my $data = named_list(
  A3 => dataframe(
    size => float([45252, 45907]),
    isdir => boolean([0, 0]),
    mode => integer([436, 436])->set_class('octmode'),
    mtime => date([1360227629.28697, 1364324320]),
    ctime => date([1543489579.69805, 1543489579.69805]),
    atime => date([1543450562.88206, 1543450563.03406]),
    uid => integer([1001, 1010]),
    gid => integer([1001, 1001]),
    uname => string(["hornik", "ligges"]),
    grname => string(["cranadmin", "cranadmin"]),
  )->set_attr('row.names' => string(["A3/A3_0.9.1.tar.gz", "A3/A3_0.9.2.tar.gz"])),

  # A3 => boolean([1, 1])
);

write_rds($data, "dat-perl.rds");
