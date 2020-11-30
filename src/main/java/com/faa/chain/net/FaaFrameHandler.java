package com.faa.chain.net;

import com.faa.utils.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class FaaFrameHandler extends ByteToMessageCodec<Frame>  {

    private static final Logger logger = Logger.getLogger(FaaFrameHandler.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Frame frame, ByteBuf out) throws Exception {
        if (frame.getVersion() != Frame.VERSION) {
            logger.error("Invalid frame version: " + frame.getVersion());
            return;
        }

        int bodySize = frame.getBodySize();
        if (bodySize < 0 || bodySize > CommonUtil.netMaxFrameBodySize) {
            logger.error("Invalid frame body size: " + bodySize);
            return;
        }

        ByteBuf buf = out.alloc().buffer(Frame.HEADER_SIZE + bodySize);
        frame.writeHeader(buf);
        buf.writeBytes(frame.getBody());

        ctx.write(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < Frame.HEADER_SIZE) {
            return;
        }

        int readerIndex = in.readerIndex();
        Frame frame = Frame.readHeader(in);

        if (frame.getVersion() != Frame.VERSION) {
            throw new IOException("Invalid frame version: " + frame.getVersion());
        }

        int bodySize = frame.getBodySize();
        if (bodySize < 0 || bodySize > CommonUtil.netMaxFrameBodySize) {
            throw new IOException("Invalid frame body size: " + bodySize);
        }

        if (in.readableBytes() < bodySize) {
            in.readerIndex(readerIndex);
        } else {
            byte[] body = new byte[bodySize];
            in.readBytes(body);
            frame.setBody(body);

            out.add(frame);
        }
    }
}
