using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Storage;

namespace ChakraBridge
{
    public sealed class ChakraHost
    {
        private JavaScriptSourceContext currentSourceContext = JavaScriptSourceContext.FromIntPtr(IntPtr.Zero);
        private readonly JavaScriptRuntime runtime;
        private JavaScriptValue promiseCallback;
        private readonly JavaScriptContext context;
        

        public ChakraHost()
        {
            var code = Native.JsCreateRuntime(JavaScriptRuntimeAttributes.None, null, out runtime);
            if ( code!= JavaScriptErrorCode.NoError)
            {
                throw new Exception("failed to create runtime.");
            }

            code = Native.JsCreateContext(runtime, out context);
            if (code != JavaScriptErrorCode.NoError)
                throw new Exception("failed to create execution context.");

            ResetContext();

            // ES6 Promise callback
            JavaScriptPromiseContinuationCallback promiseContinuationCallback =
                delegate (JavaScriptValue task, IntPtr callbackState)
                {
                    promiseCallback = task;
                };

            code = Native.JsSetPromiseContinuationCallback(promiseContinuationCallback, IntPtr.Zero);
            if (code!=JavaScriptErrorCode.NoError)
                throw new Exception("failed to setup callback for ES6 Promise");

            // Projections
            code = Native.JsProjectWinRTNamespace("ChakraBridge");
            if (code != JavaScriptErrorCode.NoError)
                throw new Exception("failed to project ChakraBridge namespace.");



            //SetTimeoutJavaScriptNativeFunction = SetTimeout.SetTimeoutJavaScriptNativeFunction;
            //DefineHostCallback("setTimeout", SetTimeoutJavaScriptNativeFunction);
            //ProjectObjectToGlobal(new Console(), "console");

            //#if DEBUG
            //            // Debug
            //            if (Native.JsStartDebugging() != JavaScriptErrorCode.NoError)
            //                throw new Exception("failed to start debugging.");
            //#endif
        }

        private void ResetContext() {
            if (Native.JsSetCurrentContext(context) != JavaScriptErrorCode.NoError)
                throw new Exception("failed to set current context.");
        }
        

        public void ProjectNamespace(string namespaceName)
        {
            ResetContext();

            if (Native.JsProjectWinRTNamespace(namespaceName) != JavaScriptErrorCode.NoError)
                throw new Exception($"failed to project {namespaceName} namespace.");
        }

        public string RunScript(string script)
        {
            ResetContext();

            IntPtr returnValue;

            JavaScriptValue result;

            if (Native.JsRunScript(script, currentSourceContext++, "", out result) != JavaScriptErrorCode.NoError)
            {
                // Get error message and clear exception
                JavaScriptValue exception;
                if (Native.JsGetAndClearException(out exception) != JavaScriptErrorCode.NoError)
                    throw new Exception("failed to get and clear exception");

                JavaScriptPropertyId messageName;
                if (Native.JsGetPropertyIdFromName("message",
                    out messageName) != JavaScriptErrorCode.NoError)
                    throw new Exception("failed to get error message id");

                JavaScriptValue messageValue;
                if (Native.JsGetProperty(exception, messageName, out messageValue)
                    != JavaScriptErrorCode.NoError)
                    throw new Exception("failed to get error message");

                IntPtr message;
                UIntPtr length;
                if (Native.JsStringToPointer(messageValue, out message, out length) != JavaScriptErrorCode.NoError)
                    throw new Exception("failed to convert error message");

                return Marshal.PtrToStringUni(message);
            }

            // Execute promise tasks stored in promiseCallback 
            while (promiseCallback.IsValid)
            {
                JavaScriptValue task = promiseCallback;
                promiseCallback = JavaScriptValue.Invalid;
                JavaScriptValue promiseResult;
                Native.JsCallFunction(task, null, 0, out promiseResult);
            }

            // Convert the return value.
            JavaScriptValue stringResult;
            UIntPtr stringLength;
            if (Native.JsConvertValueToString(result, out stringResult) != JavaScriptErrorCode.NoError)
                throw new Exception("failed to convert value to string.");
            if (Native.JsStringToPointer(stringResult, out returnValue, out stringLength) !=
                JavaScriptErrorCode.NoError)
                throw new Exception("failed to convert return value.");

            return Marshal.PtrToStringUni(returnValue);
        }

        public string ProjectObjectToGlobal(string name, object objectToProject)
        {
            ResetContext();

            JavaScriptValue value;
            if (Native.JsInspectableToObject(objectToProject, out value) != JavaScriptErrorCode.NoError)
                return $"failed to project {name} object";

            DefineHostProperty(name, value);

            return "NoError";
        }
        

        public JavaScriptValue CallFunction(string name, params object[] parameters)
        {
            ResetContext();

            JavaScriptValue globalObject;
            Native.JsGetGlobalObject(out globalObject);

            JavaScriptPropertyId functionId = JavaScriptPropertyId.FromString(name);

            var function = globalObject.GetProperty(functionId);

            // Parameters
            var javascriptParameters = new List<JavaScriptValue>();

            javascriptParameters.Add(globalObject); // this value

            foreach (var parameter in parameters)
            {
                var parameterType = parameter.GetType().Name;
                switch (parameterType)
                {
                    case "Int32":
                        javascriptParameters.Add(JavaScriptValue.FromInt32((int)parameter));
                        break;
                    case "Double":
                        javascriptParameters.Add(JavaScriptValue.FromDouble((double)parameter));
                        break;
                    case "Boolean":
                        javascriptParameters.Add(JavaScriptValue.FromBoolean((bool)parameter));
                        break;
                    case "String":
                        javascriptParameters.Add(JavaScriptValue.FromString((string)parameter));
                        break;
                    default:
                        throw new Exception("Not supported type: " + parameterType);
                }
            }

            // call function
            return function.CallFunction(javascriptParameters.ToArray());
        }

        public void RegisterFunction(string function, Action<JavaScriptValue[]> callback) {
            ResetContext();
            
            DefineHostCallback(function,(callee, isConstructCall,arguments, argumentCount, callbackData)=> {
                callback(arguments);
                return JavaScriptValue.True;
            });
        }
        

        // Private tools
        private static void DefineHostCallback(string callbackName, JavaScriptNativeFunction callback)
        {
            JavaScriptValue globalObject;
            Native.JsGetGlobalObject(out globalObject);

            JavaScriptPropertyId propertyId = JavaScriptPropertyId.FromString(callbackName);
            JavaScriptValue function = JavaScriptValue.CreateFunction(callback, IntPtr.Zero);

            globalObject.SetProperty(propertyId, function, true);

            uint refCount;
            Native.JsAddRef(function, out refCount);
        }

        private static void DefineHostProperty(string callbackName, JavaScriptValue value)
        {
            JavaScriptValue globalObject;
            Native.JsGetGlobalObject(out globalObject);

            JavaScriptPropertyId propertyId = JavaScriptPropertyId.FromString(callbackName);
            globalObject.SetProperty(propertyId, value, true);

            uint refCount;
            Native.JsAddRef(value, out refCount);
        }
    }
}