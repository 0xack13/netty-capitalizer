import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;

public class clientNetty {
    public static void main( String[] args ) throws Exception
    {
        EventLoopGroup group = new NioEventLoopGroup();
        try
        {
            new ServerBootstrap()
                    .group( group )
                    .channel( NioServerSocketChannel.class )
                    .childHandler( new Init() )
                    .bind( 1337 ).sync().channel().closeFuture().sync();
        }
        finally
        {
            group.shutdownGracefully();
        }
    }

    private static class Init extends ChannelInitializer
    {
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast( new ShoutyHandler() );
        }
    }

    private static class ShoutyHandler extends ChannelInboundHandlerAdapter
    {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg )
        {
            try
            {
                Charset utf8 = CharsetUtil.UTF_8;
                String in = ( (ByteBuf)msg ).toString( utf8 );
                String out = in.toUpperCase(); // Shout!
                ctx.writeAndFlush( Unpooled.copiedBuffer( out, utf8 ) );
            }
            finally
            {
                ReferenceCountUtil.release( msg );
            }
        }

        @Override
        public void exceptionCaught(
                ChannelHandlerContext ctx, Throwable cause )
        {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
