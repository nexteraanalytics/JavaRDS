#!/usr/bin/python


# See https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats for the format definition


# Shortcuts
def date(i):
    return Float(i).set_class(["POSIXct", "POSIXt"])


def named_list(d: dict):
    k = d.keys()
    v = d.values()
    return List(v).set_attr("names", String(k))


def data_frame(d: dict):
    NA_INT = 1 << 31

    df = Pairlist(d)
    df.set_class(["data.frame"]).set_attr('row.names', Integer([NA_INT, -1]))
    return df


#####################################

def pack(fmt, *data):
    from struct import pack as struct_pack
    return struct_pack(f">{fmt}", *data)


class RThing:

    def __init__(self, data, attrs=None):
        self.data = data
        self.attrs: Pairlist = attrs
        self._type: int = 0

    def pack_flags(self):
        if self.type() == 0:
            return b''

        flags = self.type()

        if self.attrs is not None:
            flags |= 1 << 9

            if self.attrs.has_key("class"):
                flags |= 1 << 8

        return pack("i", flags)

    def pack_attrs(self):
        return self.attrs.pack() if self.attrs is not None else b''

    def pack(self):
        return self.pack_flags() + self.serialize() + self.pack_attrs()

    def set_attr(self, key, val):
        if self.attrs is None:
            self.attrs = Pairlist()
        self.attrs.set(key, val)
        return self

    def set_class(self, cls):
        self.set_attr('class', String(cls))
        return self

    def serialize(self):
        raise Exception("Unimplemented data")

    def type(self):
        return self._type


class Integer(RThing):
    def type(self): return 0o15

    def serialize(self):
        out = pack("i", len(self.data))
        for x in self.data:
            out += pack("i", x)
        return out


class List(RThing):
    def type(self): return 0x13

    def serialize(self):
        return pack("i", len(self.data)) + b''.join(x.pack() for x in self.data)


class Float(RThing):
    def type(self): return 0o16

    def serialize(self):
        out = pack("i", len(self.data))
        for x in self.data:
            out += pack("d", x)
        return out


class Boolean(RThing):
    def type(self): return 0o12

    def serialize(self):
        out = pack("i", len(self.data))
        for x in self.data:
            out += pack("l", x)
        return out


class String(RThing):
    STRING_START = 0o1000011  # Probably an encoding marker?

    def type(self): return 0o20

    def serialize(self):
        out = pack("i", len(self.data))
        for x in self.data:
            out += pack("i i", self.STRING_START, len(x)) + x.encode()
        return out


class Pairlist(RThing):
    # This is used for attributes too

    def __init__(self, data=None):
        if data is None:
            data = {}
        super().__init__(data)

    def serialize(self):
        out = b''
        for k, v in self.data.items():
            out += pack("i", 0o2002)
            out += pack("i i i", 1, String.STRING_START, len(k)) + k.encode('UTF-8')  # name
            out += v.pack()  # value
        out += Null().pack()
        return out

    def has_key(self, key):
        return key in self.data

    def set(self, key, val):
        self.data[key] = val


class Null(RThing):
    def __init__(self): super().__init__(None)

    def type(self): return 0o376

    def serialize(self): return b''


def vers(v, p, s):
    return (v << 16) | (p << 8) | s


def write_rds(df, file):
    with open(file, "bw") as fh:
        fh.write(b"X\n")  # Magic "number"
        fh.write(pack("3i",
                      2,  # Version of RDS spec
                      vers(3, 5, 1),  # Version of R which wrote the file
                      vers(2, 3, 0)))  # Minimal version of R needed to read the format

        fh.write(df.pack())

    # print fh integer([5]).pack();
    # print fh bless([1, 0], 'boolean').pack();
    # print fh string.new(["bar"]).pack();
    # print fh float.new([9]).pack;
    # print fh list([integer([5]), integer([6])], pairlist([names => string(['a', 'b'])])).pack;
    # print fh named_list(a => integer([5]), b => integer([6])).add_attr(foo => integer([5])).pack;
    # print fh pairlist([food => string(["barf"]), x => string(["y"])]).pack;
    # print fh null().pack;
    # print fh date([1543489579.69805, 1543489579.69805]).pack;
    # print fh dataframe(a => integer([5]), b => string(['foo'])).pack;


######################################


dat = named_list({
    "A3": data_frame({
        "size": Float([45252, 45907]),
        "isdir": Boolean([0, 0]),
        "mode": Integer([436, 436]).set_class(['octmode']),
        "mtime": date([1360227629.28697, 1364324320]),
        "ctime": date([1543489579.69805, 1543489579.69805]),
        "atime": date([1543450562.88206, 1543450563.03406]),
        "uid": Integer([1001, 1010]),
        "gid": Integer([1001, 1001]),
        "uname": String(["hornik", "ligges"]),
        "grname": String(["cranadmin", "cranadmin"])
    }).set_attr('row.names', String(["A3/A3_0.9.1.tar.gz", "A3/A3_0.9.2.tar.gz"]))
    # "A3": Boolean([1, 1])
})

write_rds(dat, "dat-python.rds")
