window.weixin={
    invoke:function(func,args){
        app.invoke('weixin',func,args);
    },
    //
    getInfo:function(obj){
        weixin.invoke('getInfo',{
            callback:nextInvokeCallback(obj.callback)
        })
    },
}