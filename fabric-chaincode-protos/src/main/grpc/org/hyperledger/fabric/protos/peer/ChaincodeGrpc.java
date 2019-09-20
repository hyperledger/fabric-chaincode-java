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
 * Chaincode as a server - peer establishes a connection to the chaincode as a client
 * Currently only supports a stream connection.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.23.0)",
    comments = "Source: peer/chaincode_shim.proto")
public final class ChaincodeGrpc {

  private ChaincodeGrpc() {}

  public static final String SERVICE_NAME = "protos.Chaincode";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage,
      org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> getConnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Connect",
      requestType = org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.class,
      responseType = org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage,
      org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> getConnectMethod() {
    io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage, org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> getConnectMethod;
    if ((getConnectMethod = ChaincodeGrpc.getConnectMethod) == null) {
      synchronized (ChaincodeGrpc.class) {
        if ((getConnectMethod = ChaincodeGrpc.getConnectMethod) == null) {
          ChaincodeGrpc.getConnectMethod = getConnectMethod =
              io.grpc.MethodDescriptor.<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage, org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Connect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.getDefaultInstance()))
              .setSchemaDescriptor(new ChaincodeMethodDescriptorSupplier("Connect"))
              .build();
        }
      }
    }
    return getConnectMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChaincodeStub newStub(io.grpc.Channel channel) {
    return new ChaincodeStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChaincodeBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ChaincodeBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChaincodeFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ChaincodeFutureStub(channel);
  }

  /**
   * <pre>
   * Chaincode as a server - peer establishes a connection to the chaincode as a client
   * Currently only supports a stream connection.
   * </pre>
   */
  public static abstract class ChaincodeImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> connect(
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> responseObserver) {
      return asyncUnimplementedStreamingCall(getConnectMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getConnectMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage,
                org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage>(
                  this, METHODID_CONNECT)))
          .build();
    }
  }

  /**
   * <pre>
   * Chaincode as a server - peer establishes a connection to the chaincode as a client
   * Currently only supports a stream connection.
   * </pre>
   */
  public static final class ChaincodeStub extends io.grpc.stub.AbstractStub<ChaincodeStub> {
    private ChaincodeStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChaincodeStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaincodeStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChaincodeStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> connect(
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getConnectMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * Chaincode as a server - peer establishes a connection to the chaincode as a client
   * Currently only supports a stream connection.
   * </pre>
   */
  public static final class ChaincodeBlockingStub extends io.grpc.stub.AbstractStub<ChaincodeBlockingStub> {
    private ChaincodeBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChaincodeBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaincodeBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChaincodeBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   * Chaincode as a server - peer establishes a connection to the chaincode as a client
   * Currently only supports a stream connection.
   * </pre>
   */
  public static final class ChaincodeFutureStub extends io.grpc.stub.AbstractStub<ChaincodeFutureStub> {
    private ChaincodeFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChaincodeFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaincodeFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChaincodeFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_CONNECT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ChaincodeImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ChaincodeImplBase serviceImpl, int methodId) {
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
        case METHODID_CONNECT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.connect(
              (io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ChaincodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ChaincodeBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.hyperledger.fabric.protos.peer.ChaincodeShim.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Chaincode");
    }
  }

  private static final class ChaincodeFileDescriptorSupplier
      extends ChaincodeBaseDescriptorSupplier {
    ChaincodeFileDescriptorSupplier() {}
  }

  private static final class ChaincodeMethodDescriptorSupplier
      extends ChaincodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ChaincodeMethodDescriptorSupplier(String methodName) {
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
      synchronized (ChaincodeGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ChaincodeFileDescriptorSupplier())
              .addMethod(getConnectMethod())
              .build();
        }
      }
    }
    return result;
  }
}
