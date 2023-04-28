package org.apache.iotdb.db.metadata.mnode.mem.impl;

import org.apache.iotdb.db.metadata.view.viewExpression.ViewExpression;
import org.apache.iotdb.tsfile.encoding.encoder.Encoder;
import org.apache.iotdb.tsfile.file.metadata.enums.CompressionType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSEncoding;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;
import org.apache.iotdb.tsfile.write.schema.IMeasurementSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogicalViewSchema
    implements IMeasurementSchema, Comparable<LogicalViewSchema>, Serializable {

  private String measurementId;

  private ViewExpression expression;

  public LogicalViewSchema(String measurementId, ViewExpression expression) {
    this.measurementId = measurementId;
    this.expression = expression;
  }

  @Override
  public int compareTo(LogicalViewSchema o) {
    if (equals(o)) {
      return 0;
    } else {
      return this.measurementId.compareTo(o.measurementId);
    }
  }

  @Override
  public String getMeasurementId() {
    return this.measurementId;
  }

  @Override
  public CompressionType getCompressor() {
    // TODO: CRTODO:add new CompressionType
    return CompressionType.UNCOMPRESSED;
  }

  @Override
  public TSEncoding getEncodingType() {
    // TODO: CRTODO: add new TSEncoding
    return TSEncoding.PLAIN;
  }

  @Override
  public TSDataType getType() {
    // TODO: CRTODO: use a dynamic method to compute data type
    return TSDataType.BOOLEAN;
  }

  @Override
  public byte getTypeInByte() {
    return TSDataType.BOOLEAN.getType();
  }

  @Override
  public void setType(TSDataType dataType) {
    // do nothing
  }

  @Override
  public TSEncoding getTimeTSEncoding() {
    // TODO: CRTODO: add new TSEncoding
    return TSEncoding.PLAIN;
  }

  @Override
  public Encoder getTimeEncoder() {
    // TODO: CRTODO: is this ok to return a null value?
    return null;
  }

  @Override
  public Encoder getValueEncoder() {
    // TODO: CRTODO: is this ok to return a null value?
    return null;
  }

  @Override
  public Map<String, String> getProps() {
    return new HashMap<>();
  }

  @Override
  public List<String> getSubMeasurementsList() {
    throw new UnsupportedOperationException("unsupported method for LogicalViewSchema");
  }

  @Override
  public List<TSDataType> getSubMeasurementsTSDataTypeList() {
    throw new UnsupportedOperationException("unsupported method for LogicalViewSchema");
  }

  @Override
  public List<TSEncoding> getSubMeasurementsTSEncodingList() {
    throw new UnsupportedOperationException("unsupported method for LogicalViewSchema");
  }

  @Override
  public List<Encoder> getSubMeasurementsEncoderList() {
    throw new UnsupportedOperationException("unsupported method for LogicalViewSchema");
  }

  @Override
  public int getSubMeasurementIndex(String measurementId) {
    return this.measurementId.equals(measurementId) ? 0 : -1;
  }

  @Override
  public int getSubMeasurementsCount() {
    return 1;
  }

  @Override
  public boolean containsSubMeasurement(String measurementId) {
    return this.measurementId.equals(measurementId);
  }

  // region serialize and deserialize

  @Override
  public int serializedSize() {
    throw new RuntimeException(
        new UnsupportedOperationException(
            "Can not calculate the size of logical view schema before serializing."));
  }

  @Override
  public int serializeTo(ByteBuffer buffer) {
    // TODO: CRTODO: the size of buffer is not calculated!
    ReadWriteIOUtils.write(measurementId, buffer);

    ViewExpression.serialize(this.expression, buffer);
    return 0;
  }

  @Override
  public int serializeTo(OutputStream outputStream) throws IOException {
    // TODO: CRTODO: the size of buffer is not calculated!
    ReadWriteIOUtils.write(measurementId, outputStream);

    ViewExpression.serialize(this.expression, outputStream);
    return 0;
  }

  @Override
  public int partialSerializeTo(ByteBuffer buffer) {
    return this.serializeTo(buffer);
  }

  @Override
  public int partialSerializeTo(OutputStream outputStream) throws IOException {
    return this.serializeTo(outputStream);
  }

  public static LogicalViewSchema deserializeFrom(InputStream inputStream) throws IOException {
    String measurementId = ReadWriteIOUtils.readString(inputStream);

    ViewExpression expression = ViewExpression.deserialize(inputStream);

    LogicalViewSchema logicalViewSchema = new LogicalViewSchema(measurementId, expression);
    return logicalViewSchema;
  }

  public static LogicalViewSchema deserializeFrom(ByteBuffer buffer) throws IOException {
    String measurementId = ReadWriteIOUtils.readString(buffer);

    ViewExpression expression = ViewExpression.deserialize(buffer);

    LogicalViewSchema logicalViewSchema = new LogicalViewSchema(measurementId, expression);
    return logicalViewSchema;
  }

  // endregion

  public ViewExpression getExpression() {
    return this.expression;
  }

  public void setExpression(ViewExpression expression) {
    this.expression = expression;
  }
}