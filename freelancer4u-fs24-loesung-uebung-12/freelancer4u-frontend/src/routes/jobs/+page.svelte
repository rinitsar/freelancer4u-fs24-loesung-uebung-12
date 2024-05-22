<script>
  import axios from "axios";
  import { page } from "$app/stores";
  import { onMount } from "svelte";
  import { jwt_token, user, myFreelancerId } from "../../store";

  const api_root = $page.url.origin;

  let currentPage;
  let nrOfPages = 0;
  let defaultPageSize = 4;

  let earningsMin;
  let jobType;

  let jobs = [];
  let job = {
    description: null,
    earnings: null,
    jobType: null,
  };

  // Teilaufgabe 2e)
  //let myFreelancerId;

  $: {
    if ($jwt_token !== "") {
      let searchParams = $page.url.searchParams;

      if (searchParams.has("page")) {
        currentPage = searchParams.get("page");
      } else {
        currentPage = "1";
      }
      getJobs();
    }
  }

/*   onMount(() => {
    // Teilaufgabe 2e)
    // Die Freelancer ID könnte man auch im reactive statement holen,
    // dann wird der Endpoint aber unnötig oft aufgerufen.
    getMyFreelancerId();
  }); */

  function getJobs() {
    let query = "?pageSize=" + defaultPageSize + "&pageNumber=" + currentPage;

    if (earningsMin) {
      query += "&min=" + earningsMin;
    }

    if (jobType && jobType !== "ALL") {
      query += "&type=" + jobType;
    }

    var config = {
      method: "get",
      url: api_root + "/api/job" + query,
      headers: { Authorization: "Bearer " + $jwt_token },
    };

    axios(config)
      .then(function (response) {
        jobs = response.data.content;

        nrOfPages = response.data.totalPages;
      })
      .catch(function (error) {
        alert("Could not get jobs");
        console.log(error);
      });
  }

  function createJob() {
    var config = {
      method: "post",
      url: api_root + "/api/job",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + $jwt_token,
      },
      data: job,
    };

    axios(config)
      .then(function (response) {
        alert("Job created");
        getJobs();
      })
      .catch(function (error) {
        alert("Could not create Job");
        console.log(error);
      });
  }

  function assignToMe(jobId) {
    var config = {
      method: "put",
      url: api_root + "/api/service/me/assignjob?jobId=" + jobId,
      headers: { Authorization: "Bearer " + $jwt_token },
    };
    axios(config)
      .then(function (response) {
        getJobs();
      })
      .catch(function (error) {
        alert("Could not assign job to me");
        console.log(error);
      });
  }

  function completeMyJob(jobId) {
    var config = {
      method: "put",
      url: api_root + "/api/service/me/completejob?jobId=" + jobId,
      headers: { Authorization: "Bearer " + $jwt_token },
    };

    axios(config)
      .then(function (response) {
        console.log(JSON.stringify(response.data));
        getJobs();
      })
      .catch(function (error) {
        alert("Could not mark as completed");
        console.log(error);
      });
  }

  // Teilaufgabe 2e)
/*   function getMyFreelancerId() {
    var config = {
      method: "get",
      url: api_root + "/api/me/freelancer",
      headers: { Authorization: "Bearer " + $jwt_token },
    };

    axios(config)
      .then(function (response) {
        myFreelancerId = response.data.id;
        console.log(myFreelancerId);
      })
      .catch(function (error) {
        alert("Could not get Freelancer associated to current user");
        console.log(error);
      });
  } */
</script>

{#if $user.user_roles && $user.user_roles.length > 0}
  <h1 class="mt-3">Create Job</h1>
  <form class="mb-5">
    <div class="row mb-3">
      <div class="col">
        <label class="form-label" for="description">Description</label>
        <input
          bind:value={job.description}
          class="form-control"
          id="description"
          type="text"
        />
      </div>
    </div>
    <div class="row mb-3">
      <div class="col">
        <label class="form-label" for="type">Type</label>
        <select bind:value={job.jobType} class="form-select" id="type">
          <option value="OTHER">OTHER</option>
          <option value="TEST">TEST</option>
          <option value="IMPLEMENT">IMPLEMENT</option>
          <option value="REVIEW">REVIEW</option>
        </select>
      </div>
      <div class="col">
        <label class="form-label" for="earnings">Earnings</label>
        <input
          bind:value={job.earnings}
          class="form-control"
          id="earnings"
          type="number"
        />
      </div>
    </div>
    <button type="button" class="btn btn-primary" on:click={createJob}
      >Submit</button
    >
  </form>
{/if}

<h1>All Jobs</h1>
<div class="row my-3">
  <div class="col-auto">
    <label for="" class="col-form-label">Earnings: </label>
  </div>
  <div class="col-3">
    <input
      class="form-control"
      type="number"
      placeholder="min"
      bind:value={earningsMin}
    />
  </div>
  <div class="col-auto">
    <label for="" class="col-form-label">Job Type: </label>
  </div>
  <div class="col-3">
    <select bind:value={jobType} class="form-select" id="type" type="text">
      <option value="ALL" />
      <option value="OTHER">OTHER</option>
      <option value="TEST">TEST</option>
      <option value="IMPLEMENT">IMPLEMENT</option>
      <option value="REVIEW">REVIEW</option>
    </select>
  </div>

  <div class="col-3">
    <a
      class="btn btn-primary"
      href={"/jobs?page=1&jobType=" + jobType + "&earningsMin=" + earningsMin}
      role="button">Apply</a
    >
  </div>
</div>
<table class="table">
  <thead>
    <tr>
      <th scope="col">Description</th>
      <th scope="col">Type</th>
      <th scope="col">Earnings</th>
      <th scope="col">State</th>
      <th scope="col">FreelancerId</th>
      <th scope="col">Actions</th>
    </tr>
  </thead>
  <tbody>
    {#each jobs as job}
      <tr>
        <td>{job.description}</td>
        <td>{job.jobType}</td>
        <td>{job.earnings}</td>
        <td>{job.jobState}</td>
        <td>{job.freelancerId}</td>
        <td>
          {#if job.jobState === "ASSIGNED"}
            <span class="badge bg-secondary">Assigned</span>
          {:else if job.freelancerId === null}
            <button
              type="button"
              class="btn btn-primary btn-sm"
              on:click={() => {
                assignToMe(job.id);
              }}
            >
              Assign to me
            </button>
          {/if}
          {#if job.jobState === "DONE"}
            <span class="badge bg-secondary">Done</span>
          {:else if job.jobState === "ASSIGNED" && job.freelancerId === $myFreelancerId}
            <button
              type="button"
              class="btn btn-primary btn-sm"
              on:click={() => {
                completeMyJob(job.id);
              }}
            >
              Complete Job
            </button>
          {/if}
        </td>
      </tr>
    {/each}
  </tbody>
</table>
<nav>
  <ul class="pagination">
    {#each Array(nrOfPages) as _, i}
      <li class="page-item">
        <a
          class="page-link"
          class:active={currentPage == i + 1}
          href={"/jobs?page=" + (i + 1)}
          >{i + 1}
        </a>
      </li>
    {/each}
  </ul>
</nav>
