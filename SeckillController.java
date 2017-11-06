package org.seckill.controller;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExcution;
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
 * Created by Miko on 2017/2/27.
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        List<Seckill> list = seckillService.getSeckillList();
        System.out.println(list.size()+" fadsfadsfads");
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getSeckillById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    @ResponseBody
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    public SeckillResult<Exposer> exposer(@PathVariable Long seckillId) {
        SeckillResult<Exposer> result;
        try {

            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    public SeckillResult<SeckillExcution> excute(@PathVariable("seckillId") Long seckillId,
                                                 @PathVariable("md5") String md5,
                                                 @CookieValue(value = "killPhone", required = false) Long userPhone) {
        if (userPhone == null) {
            return new SeckillResult<SeckillExcution>(false, "未注册");
        }
        SeckillResult<SeckillExcution> result;
        try {
            SeckillExcution seckillExcution = seckillService.executeSeckillProcedure(seckillId, userPhone, md5);
            result = new SeckillResult<SeckillExcution>(true, seckillExcution);
        } catch (RepeatKillException e1) {
            SeckillExcution seckillExcution = new SeckillExcution(seckillId,
                    SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExcution>(false, seckillExcution);
        } catch (SeckillCloseException e2) {
            SeckillExcution seckillExcution = new SeckillExcution(seckillId,
                    SeckillStateEnum.END);
            return new SeckillResult<SeckillExcution>(false, seckillExcution);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SeckillExcution seckillExcution = new SeckillExcution(seckillId,
                    SeckillStateEnum.INNER_ERROR);
            return new SeckillResult<SeckillExcution>(false, seckillExcution);
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    public SeckillResult<Long> time() {
        return new SeckillResult<Long>(true, new Date().getTime());
    }

}
