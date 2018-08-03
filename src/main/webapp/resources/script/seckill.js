/**
 * Created by wangdaniel on 26/4/18.
 */
//存放交互的逻辑
//javascript模块化 eg seckill.detail.init(params)
var seckill = {
    //封装秒杀相关的ajax url
    URL : {
        now: function(){
            return '/seckill/time/now';
        },
        exposer: function(seckillId){
            return '/seckill/' + seckillId + '/exposer' ;
        },
        execution: function(seckillId, md5){
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },

    //处理秒杀逻辑
    handleSeckill: function(seckillId, node) {
        //获取秒杀地址,控制显示逻辑,执行秒杀
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');

        $.post(seckill.URL.exposer(seckillId), {}, function(result){
            //post成功后，在回调函数中执行交互流程
            if(result && result['success']){
                var exposer = result['data'];
                //判断秒杀是否开始
                if(exposer['exposed']){
                    //开启秒杀，先获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log("kill url: " + killUrl);
                    //$('#killBtn').click(); 这里不用click来绑定，是因为click是永久绑定
                    //使用one是只绑定一次
                    $('#killBtn').one('click', function(){
                        //$(this) 指代这个button
                        $(this).addClass('disabled'); //1，禁用按钮
                        $.post(killUrl, {}, function(result){ //2,发送秒杀请求
                            if(result && result['success']){
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            }else{
                                node.html('<span class="label label-error">' + result['data']['stateInfo'] + '</span>');
                            }
                        })
                    });
                    node.show();
                }else{
                    //未开始
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.countDown(seckillId, now, start, end);
                }
            }else{
                console.log('result: ' + result);
            }
        });
    },

    //验证手机号
    validatePhone : function(phone){
        if(phone && phone.length == 11 && !isNaN(phone)){
            return true;
        }else{
            return false;
        }
    },

    //倒计时
    countDown : function(seckillId, nowTime, startTime, endTime){
        var seckillBox = $('#seckill-box');
        //时间判断
        if(nowTime > endTime){
            //秒杀结束
            seckillBox.html('秒杀结束');
        } else if (nowTime < startTime) {
            //秒杀未开始,计时事件绑定
            var killTime = new Date(startTime + 1000);
            seckillBox.countdown(killTime, function(event){
                //控制时间格式
                var format = event.strftime('秒杀计时: %D天 %H时 %M分 %S秒');
                seckillBox.html(format);
            }).on('finish.countdown', function(){ //时间完成后回调事件
                //获取秒杀地址,控制显示逻辑,执行秒杀
                seckill.handleSeckill(seckillId, seckillBox);
            });
        } else {
            //秒杀开始
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },


    //详情页秒杀逻辑
    detail: {
        //详情页初始化
        init : function(params){
            //手机验证和登录,计时交互
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            if(!seckill.validatePhone(killPhone)){
                //如果未登录,需要绑定手机号
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    //显示弹出层
                    show : true,
                    //禁止位置关闭
                    backdrop : 'static',
                    //键盘事件关闭
                    keyboard : false
                });
                $('#killPhoneBtn').click(function(){
                    var inputPhone = $('#killPhoneKey').val();
                    if(seckill.validatePhone(inputPhone)){
                        //把phone写入cookie
                        $.cookie('killPhone', inputPhone, { expires: 7, path: '/seckill' });
                        //验证通过刷新页面
                        window.location.reload();
                    }else{
                        //输入的手机号不合法,弹出killPhoneMessage
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误</label>').show(300);
                    }
                });
            }
            //登录ok
            //如果秒杀未开始,计时交互
            $.get(seckill.URL.now(), {}, function(result){
                if(result && result['success']){
                    var nowTime = result['data'];
                    //时间判断
                    seckill.countDown(seckillId, nowTime, startTime, endTime);
                }else{
                    console.log('result:' + result);
                }
            });
        }
    }
}
