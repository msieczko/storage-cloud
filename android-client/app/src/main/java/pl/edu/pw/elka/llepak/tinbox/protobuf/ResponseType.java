// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package pl.edu.pw.elka.llepak.tinbox.protobuf;

/**
 * Protobuf enum {@code StorageCloud.ResponseType}
 */
public enum ResponseType
    implements com.google.protobuf.Internal.EnumLite {
  /**
   * <code>NULL5 = 0;</code>
   */
  NULL5(0),
  /**
   * <code>OK = 1;</code>
   */
  OK(1),
  /**
   * <code>ERROR = 2;</code>
   */
  ERROR(2),
  /**
   * <code>LOGGED = 3;</code>
   */
  LOGGED(3),
  /**
   * <code>STAT = 4;</code>
   */
  STAT(4),
  /**
   * <code>FILES = 5;</code>
   */
  FILES(5),
  /**
   * <code>SHARED = 6;</code>
   */
  SHARED(6),
  /**
   * <code>SRV_DATA = 7;</code>
   */
  SRV_DATA(7),
  /**
   * <code>CAN_SEND = 8;</code>
   */
  CAN_SEND(8),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>NULL5 = 0;</code>
   */
  public static final int NULL5_VALUE = 0;
  /**
   * <code>OK = 1;</code>
   */
  public static final int OK_VALUE = 1;
  /**
   * <code>ERROR = 2;</code>
   */
  public static final int ERROR_VALUE = 2;
  /**
   * <code>LOGGED = 3;</code>
   */
  public static final int LOGGED_VALUE = 3;
  /**
   * <code>STAT = 4;</code>
   */
  public static final int STAT_VALUE = 4;
  /**
   * <code>FILES = 5;</code>
   */
  public static final int FILES_VALUE = 5;
  /**
   * <code>SHARED = 6;</code>
   */
  public static final int SHARED_VALUE = 6;
  /**
   * <code>SRV_DATA = 7;</code>
   */
  public static final int SRV_DATA_VALUE = 7;
  /**
   * <code>CAN_SEND = 8;</code>
   */
  public static final int CAN_SEND_VALUE = 8;


  public final int getNumber() {
    return value;
  }

  /**
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static ResponseType valueOf(int value) {
    return forNumber(value);
  }

  public static ResponseType forNumber(int value) {
    switch (value) {
      case 0: return NULL5;
      case 1: return OK;
      case 2: return ERROR;
      case 3: return LOGGED;
      case 4: return STAT;
      case 5: return FILES;
      case 6: return SHARED;
      case 7: return SRV_DATA;
      case 8: return CAN_SEND;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<ResponseType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      ResponseType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<ResponseType>() {
          public ResponseType findValueByNumber(int number) {
            return ResponseType.forNumber(number);
          }
        };

  private final int value;

  private ResponseType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:StorageCloud.ResponseType)
}

