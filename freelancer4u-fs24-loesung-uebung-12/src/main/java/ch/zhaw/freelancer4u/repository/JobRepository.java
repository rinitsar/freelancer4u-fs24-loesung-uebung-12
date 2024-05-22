package ch.zhaw.freelancer4u.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import ch.zhaw.freelancer4u.model.Job;
import ch.zhaw.freelancer4u.model.JobStateAggregation;
import ch.zhaw.freelancer4u.model.JobType;

public interface JobRepository extends MongoRepository<Job,String>{
    Page<Job> findByEarningsGreaterThan(Double earnings, Pageable pageable);
    Page<Job> findByJobType(JobType jobType, Pageable pageable);
    Page<Job> findByJobTypeAndEarningsGreaterThan(JobType jobType, Double earnings, Pageable pageable);

    @Aggregation("{$group:{_id:'$jobState',jobIds:{$push:'$_id',},count:{$count:{}}}}")
    List<JobStateAggregation> getJobStateAggregation();
}
