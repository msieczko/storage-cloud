using System;
using System.IO;
using System.Runtime.InteropServices;
using Google.Protobuf;
using StorageCloud.Desktop.Protobuf;
using File = System.IO.File;
using Type = Google.Protobuf.WellKnownTypes.Type;

namespace StorageCloud.Desktop
{
    internal class Serializer
    {
        private static void Main(string[] args)
        {
            string filename = "data.dat";

            if (args.Length == 1)
            {
                Param param;
                using (Stream stream = File.OpenRead(filename))
                {
                    param = Param.Parser.ParseFrom(stream);
                }
                Console.WriteLine(param);
            }
            else
            {
                Param param = new Param()
                {
                    ParamId = "size",
                    IParamVal = 10
                };
                using (Stream stream = File.OpenWrite(filename))
                {
                    param.WriteTo(stream);
                }
            }
        }

        internal static byte[] Serialize(IMessage message)
        {
            return message.ToByteArray();
        }
    }
}
