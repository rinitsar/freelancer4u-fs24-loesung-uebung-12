package ch.zhaw.freelancer4u.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.freelancer4u.model.Job;
import ch.zhaw.freelancer4u.model.JobCreateDTO;
import ch.zhaw.freelancer4u.model.JobStateAggregation;
import ch.zhaw.freelancer4u.model.JobType;
import ch.zhaw.freelancer4u.repository.JobRepository;
import ch.zhaw.freelancer4u.service.RoleService;

@RestController
@RequestMapping("/api")
public class JobController {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    RoleService roleService;

    @PostMapping("/job")
    public ResponseEntity<Job> createJob(@RequestBody JobCreateDTO cDTO, @AuthenticationPrincipal Jwt jwt) {
        if (!roleService.hasRole("admin", jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Job jDAO = new Job(cDTO.getDescription(), cDTO.getJobType(), cDTO.getEarnings());
        Job j = jobRepository.save(jDAO);
        return new ResponseEntity<>(j, HttpStatus.CREATED);
    }

    @GetMapping("/job")
    public ResponseEntity<Page<Job>> getAllJobs(
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) JobType type,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "2") Integer pageSize) {
        Page<Job> allJobs;
        if (min == null && type == null) {
            allJobs = jobRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        } else {
            if (min != null && type != null) {
                allJobs = jobRepository.findByJobTypeAndEarningsGreaterThan(type, min,
                        PageRequest.of(pageNumber - 1, pageSize));
            } else if (min != null) {
                allJobs = jobRepository.findByEarningsGreaterThan(min,
                        PageRequest.of(pageNumber - 1, pageSize));
            } else {
                allJobs = jobRepository.findByJobType(type, PageRequest.of(pageNumber - 1,
                        pageSize));
            }
        }
        return new ResponseEntity<>(allJobs, HttpStatus.OK);
    }

    @GetMapping("/job/aggregation/state")
    public List<JobStateAggregation> getJobStateAggregation() {
        return jobRepository.getJobStateAggregation();
    }

    @DeleteMapping("/job")
    public ResponseEntity<String> deleteAllJobs(@AuthenticationPrincipal Jwt jwt) {
        if (!roleService.hasRole("admin", jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        jobRepository.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("DELETED");
    }

}
