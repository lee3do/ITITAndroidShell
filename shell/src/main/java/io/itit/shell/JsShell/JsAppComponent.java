package io.itit.shell.JsShell;

import dagger.Component;
import io.itit.shell.ui.ShellFragment;

@FragmentScope
@Component(modules = JsAppModule.class)
public interface JsAppComponent {
    void inject(ShellFragment shellFragment);
}
