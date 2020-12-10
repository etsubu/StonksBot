package Core.Schedulers;

/**
 * Callback interface which is invoked by scheduler service
 */
public interface Schedulable {
    void invoke();
}
