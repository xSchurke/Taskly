package espresso;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.nyapp.taskly.view.AddNewTaskActivity;
import com.nyapp.taskly.view.MainActivity;
import com.nyapp.taskly.view.TaskListActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoTest {
    @Rule
    public ActivityScenarioRule<MainActivity> mainRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public ActivityScenarioRule<TaskListActivity> taskListRule =
            new ActivityScenarioRule<>(TaskListActivity.class);

    @Rule
    public ActivityScenarioRule<AddNewTaskActivity> AddTaskRule =
            new ActivityScenarioRule<>(AddNewTaskActivity.class);
}
