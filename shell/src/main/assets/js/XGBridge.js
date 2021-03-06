window.xg={
    invoke:function(func,args){
        app.invoke('XGBridge',func,args);
    },
    //
    getInfo:function(obj){
        xg.invoke('getInfo',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    setEnableDebug:function(obj){
        xg.invoke('setEnableDebug',{
            enable:obj.enable,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //appid 为整数
    startXG:function(obj){
        xg.invoke('startXG',{
            appId:obj.appId,
            appKey:obj.appKey,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    stopXG:function(obj){
        xg.invoke('stopXG',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //id 长度大于5
    bind:function(obj){
        xg.invoke('bind',{
            id:obj.id,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    unbind:function(obj){
        xg.invoke('unbind',{
            id:obj.id,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    getDeviceToken:function(obj){
        xg.invoke('getDeviceToken',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
}

