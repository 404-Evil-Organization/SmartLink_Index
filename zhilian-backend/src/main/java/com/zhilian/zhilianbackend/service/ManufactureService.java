package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.dto.request.ManufactureAddRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ManufactureListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ManufactureUpdateRequestDTO;
import com.zhilian.zhilianbackend.dto.response.ManufactureAddVO;
import com.zhilian.zhilianbackend.dto.response.ManufactureDetailVO;
import com.zhilian.zhilianbackend.dto.response.ManufactureListVO;
import com.zhilian.zhilianbackend.entity.Manufacture;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 23:30
 * @Param:
 * @Return:
 * @Description: 制造企业 Service 接口
 **/
public interface ManufactureService extends IService<Manufacture> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:30
     * @Param: requestDTO 查询请求参数
     * @Return: IPage<ManufactureListVO> 分页结果
     * @Description: 分页查询制造企业列表
     **/
    IPage<ManufactureListVO> getManufactureList(ManufactureListRequestDTO requestDTO);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:30
     * @Param: id 企业ID
     * @Return: ManufactureDetailVO 企业详情
     * @Description: 根据ID获取制造企业详情
     **/
    ManufactureDetailVO getManufactureDetail(Long id);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:30
     * @Param: requestDTO 新增企业请求参数
     * @Return: ManufactureAddVO 新增结果（返回新ID）
     * @Description: 新增制造企业
     **/
    ManufactureAddVO addManufacture(ManufactureAddRequestDTO requestDTO);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:30
     * @Param: id 企业ID
     * @Param: requestDTO 修改企业请求参数
     * @Return: void
     * @Description: 修改制造企业信息
     **/
    void updateManufacture(Long id, ManufactureUpdateRequestDTO requestDTO);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:30
     * @Param: id 企业ID
     * @Return: void
     * @Description: 删除制造企业（逻辑删除）
     **/
    void deleteManufacture(Long id);
}