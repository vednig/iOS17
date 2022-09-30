package foundation.e.blisslauncher.domain.executors

import io.reactivex.Scheduler

interface PostExecutionThread {
    val scheduler: Scheduler
}
