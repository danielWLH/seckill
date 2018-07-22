package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by linghui.wlh on 26/4/18.
 */
//@Service @Component 都可以
@Controller
// url规范: /模块/资源/{ID}/细分
@RequestMapping("/seckill")
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model){
        System.out.println("seckill_controller");
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        //list.jsp + model = ModelAndView
        return "list";  // == WEB-INF/jsp/list.jsp
    }

    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model){
        if(seckillId == null){
            return "redirect:/seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);
        if(seckill == null){
            return "forward:/seckill/list";
        }

        model.addAttribute("seckill", seckill);
        return "detail";
    }

    //ajax, return json
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable Long seckillId){
        SeckillResult<Exposer> result ;
        try{
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        }catch(Exception e){
            logger.error(e.getMessage());
            result = new SeckillResult<Exposer>(false, e.getMessage());
            return result;
        }
        return result;
    }


    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId")Long seckillId,
                                                   @PathVariable("md5")String md5,
                                                   @CookieValue(value = "killPhone", required = false) Long phone){
        if(phone == null){
            return new SeckillResult<SeckillExecution>(false, "未注册");
        }

        SeckillResult<SeckillExecution> result;

        try{
            SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch(RepeatKillException e){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(false, execution);
        }catch(SeckillCloseException e){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
            return new SeckillResult<SeckillExecution>(false, execution);
        }catch(Exception e){ //这里的异常类型可以继续细化,成三种自定义的异常,在org.seckill.exception包下面定义
            logger.error(e.getMessage());
            result = new SeckillResult<SeckillExecution>(false, e.getMessage());
        }

        return result;
    }

    /**
     * 获取系统时间
     * @return
     */
    @RequestMapping(value = "/time/now",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Long> time(){
        Date d = new Date();
        return new SeckillResult<Long>(true, d.getTime());
    }

}
