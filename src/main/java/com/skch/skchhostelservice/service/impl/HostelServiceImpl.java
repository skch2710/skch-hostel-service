package com.skch.skchhostelservice.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.skch.skchhostelservice.common.Constant;
import com.skch.skchhostelservice.dao.HostellerDAO;
import com.skch.skchhostelservice.dao.PaymentHistoryDAO;
import com.skch.skchhostelservice.dto.HostellerDTO;
import com.skch.skchhostelservice.dto.HostellerSearch;
import com.skch.skchhostelservice.dto.PaymentHistoryDTO;
import com.skch.skchhostelservice.dto.Result;
import com.skch.skchhostelservice.exception.CustomException;
import com.skch.skchhostelservice.mapper.ObjectMapper;
import com.skch.skchhostelservice.model.Hosteller;
import com.skch.skchhostelservice.model.PaymentHistory;
import com.skch.skchhostelservice.service.HostelService;
import com.skch.skchhostelservice.util.Utility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HostelServiceImpl implements HostelService {

	private ObjectMapper MAPPER = ObjectMapper.INSTANCE;

	@Autowired
	private HostellerDAO hostellerDAO;
	
	@Autowired
	private PaymentHistoryDAO paymentHistoryDAO;

	/**
	 * Save Or Update the Hosteller
	 * 
	 * @param HostellerDTO dto
	 * @return the result
	 */
	@Override
	public Result saveOrUpdateHosteller(HostellerDTO dto) {

		log.info("Starting at saveOrUpdateHosteller.....");
		Result result = null;
		Hosteller hosteller = null;
		try {
			dto.setFee(Utility.isBlank(dto.getFee()));
			dto.setVacatedDate(Utility.isBlank(dto.getVacatedDate()));
			dto.setJoiningDate(Utility.isBlank(dto.getJoiningDate()));
			
			log.info(">>>>>>"+dto);			
			result = new Result();
			if (dto.getHostellerId() == null || dto.getHostellerId() == 0) {
				hosteller = MAPPER.fromHostellerDTO(dto);
				hosteller.setCreatedDate(LocalDateTime.now());
				hosteller.setModifiedDate(LocalDateTime.now());
				hosteller.setActive(true);
				if(hosteller.getJoiningDate() == null) {
					hosteller.setJoiningDate(LocalDate.now());
				}
				hosteller = hostellerDAO.save(hosteller);

				result.setStatusCode(HttpStatus.OK.value());
				result.setSuccessMessage("Saved Successfully...");
				result.setData(hosteller);
			} else {
				Hosteller serverHosteller = hostellerDAO.findByHostellerId(dto.getHostellerId());

				hosteller = MAPPER.fromHostellerDTO(dto);
				hosteller.setActive(serverHosteller.getActive());
				hosteller.setCreatedDate(serverHosteller.getCreatedDate());
				hosteller.setCreatedById(serverHosteller.getCreatedById());
				hosteller.setVacatedDate(serverHosteller.getVacatedDate());
				hosteller.setModifiedDate(LocalDateTime.now());

				hosteller = hostellerDAO.save(hosteller);

				result.setStatusCode(HttpStatus.OK.value());
				result.setSuccessMessage("Updated Successfully...");
				result.setData(hosteller);
			}
			log.info("Ending at saveOrUpdateHosteller.....");
		} catch (Exception e) {
			log.error("Error at saveOrUpdateHosteller :: " + e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

	/**
	 * Save Or Update the PaymentHistory
	 * 
	 * @param PaymentHistoryDTO dto
	 * @return the result
	 */
	@Override
	public Result saveOrUpdatePaymentHistory(PaymentHistoryDTO dto) {
		log.info("Starting at saveOrUpdatePaymentHistory.....");
		Result result = null;
		PaymentHistory paymentHistory = null;
		try {
			dto.setFeePaid(Utility.isBlank(dto.getFeePaid()));
			dto.setFeeDue(Utility.isBlank(dto.getFeeDue()));
			dto.setFeeDate(Utility.isBlank(dto.getFeeDate()));
			result = new Result();
			if (dto.getPaymentId() == null || dto.getPaymentId() == 0) {
				paymentHistory = MAPPER.fromPaymentHistoryDTO(dto);
				paymentHistory.setCreatedDate(LocalDateTime.now());
				paymentHistory.setModifiedDate(LocalDateTime.now());
				paymentHistory = paymentHistoryDAO.save(paymentHistory);

				result.setStatusCode(HttpStatus.OK.value());
				result.setSuccessMessage("Saved Successfully...");
				result.setData(paymentHistory);
			} else {
				PaymentHistory serverPaymentHistory = paymentHistoryDAO.findByPaymentId(dto.getPaymentId());

				paymentHistory = MAPPER.fromPaymentHistoryDTO(dto);
				paymentHistory.setCreatedDate(serverPaymentHistory.getCreatedDate());
				paymentHistory.setCreatedById(serverPaymentHistory.getCreatedById());
				paymentHistory.setModifiedDate(LocalDateTime.now());

				paymentHistory = paymentHistoryDAO.save(paymentHistory);

				result.setStatusCode(HttpStatus.OK.value());
				result.setSuccessMessage("Updated Successfully...");
				result.setData(paymentHistory);
			}
			log.info("Ending at saveOrUpdatePaymentHistory.....");
		} catch (Exception e) {
			log.error("Error at saveOrUpdatePaymentHistory :: " + e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

	/**
	 * Getting All Hostellers.
	 */
	@Override
	public Result getHostellers(HostellerSearch search) {
		log.info("Starting at getHostellers.....");
		Result result = new Result();
		try {
			List<Hosteller> allHostellers = 
					hostellerDAO.findByFullNameStartingWithIgnoreCaseAndEmailIdStartingWithIgnoreCase(
							search.getFullName(),search.getEmailId());
			
			if(!allHostellers.isEmpty()) {
				List<HostellerDTO> dtoList = MAPPER.formHostelModel(allHostellers);
				result.setStatusCode(HttpStatus.OK.value());
				result.setSuccessMessage("getting Successfully...");
				result.setData(dtoList);
			}else {
				result.setStatusCode(HttpStatus.NOT_FOUND.value());
				result.setErrorMessage(Constant.NOT_FOUND);
			}
			
			log.info("Ending at getHostellers.....");
		} catch (Exception e) {
			log.error("Error at getHostellers :: " + e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

}
