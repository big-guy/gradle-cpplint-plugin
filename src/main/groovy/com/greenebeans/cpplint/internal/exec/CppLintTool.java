/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greenebeans.cpplint.internal.exec;

import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;

public class CppLintTool implements Transformer<Boolean, CppLintSpec> {
    private final ExecActionFactory execActionFactory;

    public CppLintTool(ExecActionFactory execActionFactory) {
        this.execActionFactory = execActionFactory;
    }

    @Override
    public Boolean transform(CppLintSpec cppLintSpec) {
        Action<CppLintInvocation> worker = new CppLintInvocationWorker(cppLintSpec, execActionFactory);

        for (File sourceFile : cppLintSpec.getSourceFiles()) {
            worker.execute(new CppLintInvocation(sourceFile));
        }

        return !cppLintSpec.getSourceFiles().isEmpty();
    }
}
