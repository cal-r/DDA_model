/**
 * 
 */
package simulator;

/**
 * A message passing object for tracking progress in long running tasks and
 * instructing them to cancel if required.
 * 
 * City University BSc Computing with Artificial Intelligence Project title:
 * Building a TD Simulator for Real-Time Classical Conditioning
 * 
 * @supervisor Dr. Eduardo Alonso
 * @author Jonathan Gray
 **/

public class ModelControl {
	/** Task progress. **/
	private volatile double progress;

    public int getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(int totalProgress) {
        this.totalProgress = totalProgress;
    }

    private volatile int totalProgress;
	/** Cancelled switch. **/
	private volatile boolean isCancelled;
	/** Estimated time taken for one run of the basic section of the task. **/
	private long estimatedCycleTime = 0;
	/** Number of times the cycletime has been updated. **/
	private int modCount;
	private volatile boolean isComplete;
    private boolean madeExport;

	public ModelControl() {
		progress = 0;
		isCancelled = false;
		modCount = 1;
		isComplete = false;
		estimatedCycleTime = 0;
        madeExport = false;
        totalProgress = 0;
	}

    public void madeExport(boolean made) {
        madeExport = made;
    }

    public boolean madeExport() {
        return madeExport;
    }

	/**
	 * 
	 * @return the estimated time for a single section of the underlying task.
	 */

	public long getEstimatedCycleTime() {
		return estimatedCycleTime / modCount;
	}

	/**
	 * @return the progress
	 */
	public double getProgress() {
		return progress;
	}

	/**
	 * 
	 * @param increment
	 *            to apply to progress.
	 */

	public void incrementProgress(double increment) {
		progress += increment;
	}

	/**
	 * @return true if the associated task has been cancelled.
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * @return the isComplete
	 */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * @param isCancelled
	 *            true to cancel the task this passes messages for.
	 */
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * @param isComplete
	 *            the isComplete to set
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	 * 
	 * @param time
	 *            recorded run time for a subsection of the main task.
	 */

	public void setEstimatedCycleTime(long time) {
		time = Math.max(1, time);
		modCount++;
		estimatedCycleTime += time;// = Math.max(estimatedCycleTime,time);
	}

	/**
	 * @param progress
	 *            the progress to set
	 */
	public void setProgress(double progress) {
		this.progress = progress;
	}
}