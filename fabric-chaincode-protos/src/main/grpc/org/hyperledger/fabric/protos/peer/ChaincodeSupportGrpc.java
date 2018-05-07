package org.hyperledger.fabric.protos.peer;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Interface that provides support to chaincode execution. ChaincodeContext
 * provides the context necessary for the server to respond appropriately.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.9.0)",
    comments = "Source: peer/chaincode_shim.proto")
public final class ChaincodeSupportGrpc {

  private ChaincodeSupportGrpc() {}

  public static final String SERVICE_NAME = "protos.ChaincodeSupport";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getRegisterMethod()} instead. 
  public static final io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage,
      org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> METHOD_REGISTER = getRegisterMethod();

  private static volatile io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage,
      org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> getRegisterMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage,
      org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> getRegisterMethod() {
    io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage, org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> getRegisterMethod;
    if ((getRegisterMethod = ChaincodeSupportGrpc.getRegisterMethod) == null) {
      synchronized (ChaincodeSupportGrpc.class) {
        if ((getRegisterMethod = ChaincodeSupportGrpc.getRegisterMethod) == null) {
          ChaincodeSupportGrpc.getRegisterMethod = getRegisterMethod = 
              io.grpc.MethodDescriptor.<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage, org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "protos.ChaincodeSupport", "Register"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.getDefaultInstance()))
                  .setSchemaDescriptor(new ChaincodeSupportMethodDescriptorSupplier("Register"))
                  .build();
          }
        }
     }
     return getRegisterMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChaincodeSupportStub newStub(io.grpc.Channel channel) {
    return new ChaincodeSupportStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChaincodeSupportBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ChaincodeSupportBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChaincodeSupportFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ChaincodeSupportFutureStub(channel);
  }

  /**
   * <pre>
   * Interface that provides support to chaincode execution. ChaincodeContext
   * provides the context necessary for the server to respond appropriately.
   * </pre>
   */
  public static abstract class ChaincodeSupportImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> register(
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> responseObserver) {
      return asyncUnimplementedStreamingCall(getRegisterMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRegisterMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage,
                org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage>(
                  this, METHODID_REGISTER)))
          .build();
    }
  }

  /**
   * <pre>
   * Interface that provides support to chaincode execution. ChaincodeContext
   * provides the context necessary for the server to respond appropriately.
   * </pre>
   */
  public static final class ChaincodeSupportStub extends io.grpc.stub.AbstractStub<ChaincodeSupportStub> {
    private ChaincodeSupportStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChaincodeSupportStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaincodeSupportStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChaincodeSupportStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> register(
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getRegisterMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * Interface that provides support to chaincode execution. ChaincodeContext
   * provides the context necessary for the server to respond appropriately.
   * </pre>
   */
  public static final class ChaincodeSupportBlockingStub extends io.grpc.stub.AbstractStub<ChaincodeSupportBlockingStub> {
    private ChaincodeSupportBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChaincodeSupportBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaincodeSupportBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChaincodeSupportBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   * Interface that provides support to chaincode execution. ChaincodeContext
   * provides the context necessary for the server to respond appropriately.
   * </pre>
   */
  public static final class ChaincodeSupportFutureStub extends io.grpc.stub.AbstractStub<ChaincodeSupportFutureStub> {
    private ChaincodeSupportFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChaincodeSupportFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaincodeSupportFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChaincodeSupportFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_REGISTER = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ChaincodeSupportImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ChaincodeSupportImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.register(
              (io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ChaincodeSupportBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ChaincodeSupportBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.hyperledger.fabric.protos.peer.ChaincodeShim.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ChaincodeSupport");
    }
  }

  private static final class ChaincodeSupportFileDescriptorSupplier
      extends ChaincodeSupportBaseDescriptorSupplier {
    ChaincodeSupportFileDescriptorSupplier() {}
  }

  private static final class ChaincodeSupportMethodDescriptorSupplier
      extends ChaincodeSupportBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ChaincodeSupportMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ChaincodeSupportGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ChaincodeSupportFileDescriptorSupplier())
              .addMethod(getRegisterMethod())
              .build();
        }
      }
    }
    return result;
  }
}
