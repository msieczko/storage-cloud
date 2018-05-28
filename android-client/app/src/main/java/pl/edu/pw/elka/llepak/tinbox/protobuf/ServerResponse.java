// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package pl.edu.pw.elka.llepak.tinbox.protobuf;

/**
 * Protobuf type {@code StorageCloud.ServerResponse}
 */
public  final class ServerResponse extends
    com.google.protobuf.GeneratedMessageLite<
        ServerResponse, ServerResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:StorageCloud.ServerResponse)
    ServerResponseOrBuilder {
  private ServerResponse() {
    params_ = emptyProtobufList();
    list_ = com.google.protobuf.GeneratedMessageLite.emptyProtobufList();
    fileList_ = emptyProtobufList();
    data_ = com.google.protobuf.ByteString.EMPTY;
  }
  private int bitField0_;
  public static final int TYPE_FIELD_NUMBER = 1;
  private int type_;
  /**
   * <code>optional .StorageCloud.ResponseType type = 1;</code>
   */
  public int getTypeValue() {
    return type_;
  }
  /**
   * <code>optional .StorageCloud.ResponseType type = 1;</code>
   */
  public pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType getType() {
    pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType result = pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType.forNumber(type_);
    return result == null ? pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType.UNRECOGNIZED : result;
  }
  /**
   * <code>optional .StorageCloud.ResponseType type = 1;</code>
   */
  private void setTypeValue(int value) {
      type_ = value;
  }
  /**
   * <code>optional .StorageCloud.ResponseType type = 1;</code>
   */
  private void setType(pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType value) {
    if (value == null) {
      throw new NullPointerException();
    }
    
    type_ = value.getNumber();
  }
  /**
   * <code>optional .StorageCloud.ResponseType type = 1;</code>
   */
  private void clearType() {
    
    type_ = 0;
  }

  public static final int PARAMS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<pl.edu.pw.elka.llepak.tinbox.protobuf.Param> params_;
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  public java.util.List<pl.edu.pw.elka.llepak.tinbox.protobuf.Param> getParamsList() {
    return params_;
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  public java.util.List<? extends pl.edu.pw.elka.llepak.tinbox.protobuf.ParamOrBuilder> 
      getParamsOrBuilderList() {
    return params_;
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  public int getParamsCount() {
    return params_.size();
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  public pl.edu.pw.elka.llepak.tinbox.protobuf.Param getParams(int index) {
    return params_.get(index);
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  public pl.edu.pw.elka.llepak.tinbox.protobuf.ParamOrBuilder getParamsOrBuilder(
      int index) {
    return params_.get(index);
  }
  private void ensureParamsIsMutable() {
    if (!params_.isModifiable()) {
      params_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(params_);
     }
  }

  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void setParams(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param value) {
    if (value == null) {
      throw new NullPointerException();
    }
    ensureParamsIsMutable();
    params_.set(index, value);
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void setParams(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param.Builder builderForValue) {
    ensureParamsIsMutable();
    params_.set(index, builderForValue.build());
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void addParams(pl.edu.pw.elka.llepak.tinbox.protobuf.Param value) {
    if (value == null) {
      throw new NullPointerException();
    }
    ensureParamsIsMutable();
    params_.add(value);
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void addParams(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param value) {
    if (value == null) {
      throw new NullPointerException();
    }
    ensureParamsIsMutable();
    params_.add(index, value);
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void addParams(
      pl.edu.pw.elka.llepak.tinbox.protobuf.Param.Builder builderForValue) {
    ensureParamsIsMutable();
    params_.add(builderForValue.build());
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void addParams(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param.Builder builderForValue) {
    ensureParamsIsMutable();
    params_.add(index, builderForValue.build());
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void addAllParams(
      java.lang.Iterable<? extends pl.edu.pw.elka.llepak.tinbox.protobuf.Param> values) {
    ensureParamsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, params_);
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void clearParams() {
    params_ = emptyProtobufList();
  }
  /**
   * <code>repeated .StorageCloud.Param params = 2;</code>
   */
  private void removeParams(int index) {
    ensureParamsIsMutable();
    params_.remove(index);
  }

  public static final int LIST_FIELD_NUMBER = 3;
  private com.google.protobuf.Internal.ProtobufList<String> list_;
  /**
   * <code>repeated string list = 3;</code>
   */
  public java.util.List<String> getListList() {
    return list_;
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  public int getListCount() {
    return list_.size();
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  public java.lang.String getList(int index) {
    return list_.get(index);
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  public com.google.protobuf.ByteString
      getListBytes(int index) {
    return com.google.protobuf.ByteString.copyFromUtf8(
        list_.get(index));
  }
  private void ensureListIsMutable() {
    if (!list_.isModifiable()) {
      list_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(list_);
     }
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  private void setList(
      int index, java.lang.String value) {
    if (value == null) {
    throw new NullPointerException();
  }
  ensureListIsMutable();
    list_.set(index, value);
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  private void addList(
      java.lang.String value) {
    if (value == null) {
    throw new NullPointerException();
  }
  ensureListIsMutable();
    list_.add(value);
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  private void addAllList(
      java.lang.Iterable<java.lang.String> values) {
    ensureListIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, list_);
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  private void clearList() {
    list_ = com.google.protobuf.GeneratedMessageLite.emptyProtobufList();
  }
  /**
   * <code>repeated string list = 3;</code>
   */
  private void addListBytes(
      com.google.protobuf.ByteString value) {
    if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
    ensureListIsMutable();
    list_.add(value.toStringUtf8());
  }

  public static final int FILELIST_FIELD_NUMBER = 4;
  private com.google.protobuf.Internal.ProtobufList<pl.edu.pw.elka.llepak.tinbox.protobuf.File> fileList_;
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  public java.util.List<pl.edu.pw.elka.llepak.tinbox.protobuf.File> getFileListList() {
    return fileList_;
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  public java.util.List<? extends pl.edu.pw.elka.llepak.tinbox.protobuf.FileOrBuilder> 
      getFileListOrBuilderList() {
    return fileList_;
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  public int getFileListCount() {
    return fileList_.size();
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  public pl.edu.pw.elka.llepak.tinbox.protobuf.File getFileList(int index) {
    return fileList_.get(index);
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  public pl.edu.pw.elka.llepak.tinbox.protobuf.FileOrBuilder getFileListOrBuilder(
      int index) {
    return fileList_.get(index);
  }
  private void ensureFileListIsMutable() {
    if (!fileList_.isModifiable()) {
      fileList_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(fileList_);
     }
  }

  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void setFileList(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File value) {
    if (value == null) {
      throw new NullPointerException();
    }
    ensureFileListIsMutable();
    fileList_.set(index, value);
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void setFileList(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File.Builder builderForValue) {
    ensureFileListIsMutable();
    fileList_.set(index, builderForValue.build());
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void addFileList(pl.edu.pw.elka.llepak.tinbox.protobuf.File value) {
    if (value == null) {
      throw new NullPointerException();
    }
    ensureFileListIsMutable();
    fileList_.add(value);
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void addFileList(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File value) {
    if (value == null) {
      throw new NullPointerException();
    }
    ensureFileListIsMutable();
    fileList_.add(index, value);
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void addFileList(
      pl.edu.pw.elka.llepak.tinbox.protobuf.File.Builder builderForValue) {
    ensureFileListIsMutable();
    fileList_.add(builderForValue.build());
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void addFileList(
      int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File.Builder builderForValue) {
    ensureFileListIsMutable();
    fileList_.add(index, builderForValue.build());
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void addAllFileList(
      java.lang.Iterable<? extends pl.edu.pw.elka.llepak.tinbox.protobuf.File> values) {
    ensureFileListIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, fileList_);
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void clearFileList() {
    fileList_ = emptyProtobufList();
  }
  /**
   * <code>repeated .StorageCloud.File fileList = 4;</code>
   */
  private void removeFileList(int index) {
    ensureFileListIsMutable();
    fileList_.remove(index);
  }

  public static final int DATA_FIELD_NUMBER = 5;
  private com.google.protobuf.ByteString data_;
  /**
   * <code>optional bytes data = 5;</code>
   */
  public com.google.protobuf.ByteString getData() {
    return data_;
  }
  /**
   * <code>optional bytes data = 5;</code>
   */
  private void setData(com.google.protobuf.ByteString value) {
    if (value == null) {
    throw new NullPointerException();
  }
  
    data_ = value;
  }
  /**
   * <code>optional bytes data = 5;</code>
   */
  private void clearData() {
    
    data_ = getDefaultInstance().getData();
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (type_ != pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType.NULL5.getNumber()) {
      output.writeEnum(1, type_);
    }
    for (int i = 0; i < params_.size(); i++) {
      output.writeMessage(2, params_.get(i));
    }
    for (int i = 0; i < list_.size(); i++) {
      output.writeString(3, list_.get(i));
    }
    for (int i = 0; i < fileList_.size(); i++) {
      output.writeMessage(4, fileList_.get(i));
    }
    if (!data_.isEmpty()) {
      output.writeBytes(5, data_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;

    size = 0;
    if (type_ != pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType.NULL5.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, type_);
    }
    for (int i = 0; i < params_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, params_.get(i));
    }
    {
      int dataSize = 0;
      for (int i = 0; i < list_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeStringSizeNoTag(list_.get(i));
      }
      size += dataSize;
      size += 1 * getListList().size();
    }
    for (int i = 0; i < fileList_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, fileList_.get(i));
    }
    if (!data_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(5, data_);
    }
    memoizedSerializedSize = size;
    return size;
  }

  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  /**
   * Protobuf type {@code StorageCloud.ServerResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:StorageCloud.ServerResponse)
      pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponseOrBuilder {
    // Construct using pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional .StorageCloud.ResponseType type = 1;</code>
     */
    public int getTypeValue() {
      return instance.getTypeValue();
    }
    /**
     * <code>optional .StorageCloud.ResponseType type = 1;</code>
     */
    public Builder setTypeValue(int value) {
      copyOnWrite();
      instance.setTypeValue(value);
      return this;
    }
    /**
     * <code>optional .StorageCloud.ResponseType type = 1;</code>
     */
    public pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType getType() {
      return instance.getType();
    }
    /**
     * <code>optional .StorageCloud.ResponseType type = 1;</code>
     */
    public Builder setType(pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType value) {
      copyOnWrite();
      instance.setType(value);
      return this;
    }
    /**
     * <code>optional .StorageCloud.ResponseType type = 1;</code>
     */
    public Builder clearType() {
      copyOnWrite();
      instance.clearType();
      return this;
    }

    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public java.util.List<pl.edu.pw.elka.llepak.tinbox.protobuf.Param> getParamsList() {
      return java.util.Collections.unmodifiableList(
          instance.getParamsList());
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public int getParamsCount() {
      return instance.getParamsCount();
    }/**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public pl.edu.pw.elka.llepak.tinbox.protobuf.Param getParams(int index) {
      return instance.getParams(index);
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder setParams(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param value) {
      copyOnWrite();
      instance.setParams(index, value);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder setParams(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param.Builder builderForValue) {
      copyOnWrite();
      instance.setParams(index, builderForValue);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder addParams(pl.edu.pw.elka.llepak.tinbox.protobuf.Param value) {
      copyOnWrite();
      instance.addParams(value);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder addParams(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param value) {
      copyOnWrite();
      instance.addParams(index, value);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder addParams(
        pl.edu.pw.elka.llepak.tinbox.protobuf.Param.Builder builderForValue) {
      copyOnWrite();
      instance.addParams(builderForValue);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder addParams(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.Param.Builder builderForValue) {
      copyOnWrite();
      instance.addParams(index, builderForValue);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder addAllParams(
        java.lang.Iterable<? extends pl.edu.pw.elka.llepak.tinbox.protobuf.Param> values) {
      copyOnWrite();
      instance.addAllParams(values);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder clearParams() {
      copyOnWrite();
      instance.clearParams();
      return this;
    }
    /**
     * <code>repeated .StorageCloud.Param params = 2;</code>
     */
    public Builder removeParams(int index) {
      copyOnWrite();
      instance.removeParams(index);
      return this;
    }

    /**
     * <code>repeated string list = 3;</code>
     */
    public java.util.List<String>
        getListList() {
      return java.util.Collections.unmodifiableList(
          instance.getListList());
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public int getListCount() {
      return instance.getListCount();
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public java.lang.String getList(int index) {
      return instance.getList(index);
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public com.google.protobuf.ByteString
        getListBytes(int index) {
      return instance.getListBytes(index);
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public Builder setList(
        int index, java.lang.String value) {
      copyOnWrite();
      instance.setList(index, value);
      return this;
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public Builder addList(
        java.lang.String value) {
      copyOnWrite();
      instance.addList(value);
      return this;
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public Builder addAllList(
        java.lang.Iterable<java.lang.String> values) {
      copyOnWrite();
      instance.addAllList(values);
      return this;
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public Builder clearList() {
      copyOnWrite();
      instance.clearList();
      return this;
    }
    /**
     * <code>repeated string list = 3;</code>
     */
    public Builder addListBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.addListBytes(value);
      return this;
    }

    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public java.util.List<pl.edu.pw.elka.llepak.tinbox.protobuf.File> getFileListList() {
      return java.util.Collections.unmodifiableList(
          instance.getFileListList());
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public int getFileListCount() {
      return instance.getFileListCount();
    }/**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public pl.edu.pw.elka.llepak.tinbox.protobuf.File getFileList(int index) {
      return instance.getFileList(index);
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder setFileList(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File value) {
      copyOnWrite();
      instance.setFileList(index, value);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder setFileList(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File.Builder builderForValue) {
      copyOnWrite();
      instance.setFileList(index, builderForValue);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder addFileList(pl.edu.pw.elka.llepak.tinbox.protobuf.File value) {
      copyOnWrite();
      instance.addFileList(value);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder addFileList(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File value) {
      copyOnWrite();
      instance.addFileList(index, value);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder addFileList(
        pl.edu.pw.elka.llepak.tinbox.protobuf.File.Builder builderForValue) {
      copyOnWrite();
      instance.addFileList(builderForValue);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder addFileList(
        int index, pl.edu.pw.elka.llepak.tinbox.protobuf.File.Builder builderForValue) {
      copyOnWrite();
      instance.addFileList(index, builderForValue);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder addAllFileList(
        java.lang.Iterable<? extends pl.edu.pw.elka.llepak.tinbox.protobuf.File> values) {
      copyOnWrite();
      instance.addAllFileList(values);
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder clearFileList() {
      copyOnWrite();
      instance.clearFileList();
      return this;
    }
    /**
     * <code>repeated .StorageCloud.File fileList = 4;</code>
     */
    public Builder removeFileList(int index) {
      copyOnWrite();
      instance.removeFileList(index);
      return this;
    }

    /**
     * <code>optional bytes data = 5;</code>
     */
    public com.google.protobuf.ByteString getData() {
      return instance.getData();
    }
    /**
     * <code>optional bytes data = 5;</code>
     */
    public Builder setData(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setData(value);
      return this;
    }
    /**
     * <code>optional bytes data = 5;</code>
     */
    public Builder clearData() {
      copyOnWrite();
      instance.clearData();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:StorageCloud.ServerResponse)
  }
  protected final Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      Object arg0, Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse();
      }
      case IS_INITIALIZED: {
        return DEFAULT_INSTANCE;
      }
      case MAKE_IMMUTABLE: {
        params_.makeImmutable();
        list_.makeImmutable();
        fileList_.makeImmutable();
        return null;
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case VISIT: {
        Visitor visitor = (Visitor) arg0;
        pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse other = (pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse) arg1;
        type_ = visitor.visitInt(type_ != 0, type_,    other.type_ != 0, other.type_);
        params_= visitor.visitList(params_, other.params_);
        list_= visitor.visitList(list_, other.list_);
        fileList_= visitor.visitList(fileList_, other.fileList_);
        data_ = visitor.visitByteString(data_ != com.google.protobuf.ByteString.EMPTY, data_,
            other.data_ != com.google.protobuf.ByteString.EMPTY, other.data_);
        if (visitor == com.google.protobuf.GeneratedMessageLite.MergeFromVisitor
            .INSTANCE) {
          bitField0_ |= other.bitField0_;
        }
        return this;
      }
      case MERGE_FROM_STREAM: {
        com.google.protobuf.CodedInputStream input =
            (com.google.protobuf.CodedInputStream) arg0;
        com.google.protobuf.ExtensionRegistryLite extensionRegistry =
            (com.google.protobuf.ExtensionRegistryLite) arg1;
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              default: {
                if (!input.skipField(tag)) {
                  done = true;
                }
                break;
              }
              case 8: {
                int rawValue = input.readEnum();

                type_ = rawValue;
                break;
              }
              case 18: {
                if (!params_.isModifiable()) {
                  params_ =
                      com.google.protobuf.GeneratedMessageLite.mutableCopy(params_);
                }
                params_.add(
                    input.readMessage(pl.edu.pw.elka.llepak.tinbox.protobuf.Param.parser(), extensionRegistry));
                break;
              }
              case 26: {
                String s = input.readStringRequireUtf8();
                if (!list_.isModifiable()) {
                  list_ =
                      com.google.protobuf.GeneratedMessageLite.mutableCopy(list_);
                }
                list_.add(s);
                break;
              }
              case 34: {
                if (!fileList_.isModifiable()) {
                  fileList_ =
                      com.google.protobuf.GeneratedMessageLite.mutableCopy(fileList_);
                }
                fileList_.add(
                    input.readMessage(pl.edu.pw.elka.llepak.tinbox.protobuf.File.parser(), extensionRegistry));
                break;
              }
              case 42: {

                data_ = input.readBytes();
                break;
              }
            }
          }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw new RuntimeException(e.setUnfinishedMessage(this));
        } catch (java.io.IOException e) {
          throw new RuntimeException(
              new com.google.protobuf.InvalidProtocolBufferException(
                  e.getMessage()).setUnfinishedMessage(this));
        } finally {
        }
      }
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        if (PARSER == null) {    synchronized (pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse.class) {
            if (PARSER == null) {
              PARSER = new DefaultInstanceBasedParser(DEFAULT_INSTANCE);
            }
          }
        }
        return PARSER;
      }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:StorageCloud.ServerResponse)
  private static final pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new ServerResponse();
    DEFAULT_INSTANCE.makeImmutable();
  }

  public static pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ServerResponse> PARSER;

  public static com.google.protobuf.Parser<ServerResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

