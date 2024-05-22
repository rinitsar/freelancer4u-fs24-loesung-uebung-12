package ch.zhaw.freelancer4u.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.zhaw.freelancer4u.model.Freelancer;
import ch.zhaw.freelancer4u.model.FreelancerCreateDTO;
import ch.zhaw.freelancer4u.repository.FreelancerRepository;
import ch.zhaw.freelancer4u.service.RoleService;

@RestController
@RequestMapping("/api")
public class FreelancerController {

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    RoleService roleService;

    @PostMapping("/freelancer")
    public ResponseEntity<Freelancer> createFreelancer(
            @RequestBody FreelancerCreateDTO fDTO, @AuthenticationPrincipal Jwt jwt) {
        if (!roleService.hasRole("admin", jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Freelancer fDAO = new Freelancer(fDTO.getEmail(), fDTO.getName());
        Freelancer f = freelancerRepository.save(fDAO);
        return new ResponseEntity<>(f, HttpStatus.CREATED);
    }

    @GetMapping("/freelancer")
    public ResponseEntity<Page<Freelancer>> getAllFreelancer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "2") Integer pageSize) {
        if (!roleService.hasRole("admin", jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Page<Freelancer> allFree = freelancerRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        return new ResponseEntity<>(allFree, HttpStatus.OK);
    }

    @GetMapping("/freelancer/{id}")
    public ResponseEntity<Freelancer> getFreelancerById(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        if (!roleService.hasRole("admin", jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<Freelancer> optFreelancer = freelancerRepository.findById(id);
        if (optFreelancer.isPresent()) {
            return new ResponseEntity<>(optFreelancer.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/me/freelancer")
    public ResponseEntity<Freelancer> getMyFreelancerId(@AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        Freelancer freelancer = freelancerRepository.findFirstByEmail(userEmail);
        if (freelancer != null) {
            return new ResponseEntity<>(freelancer, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
