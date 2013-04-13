<?cs # Table of contents for Dev Guide.

       For each document available in translation, add an localized title to this TOC.
       Do not add localized title for docs not available in translation.
       Below are template spans for adding localized doc titles. Please ensure that
       localized titles are added in the language order specified below.
?>
<ul id="nav">
  <!--  Walkthrough for Developers -- quick overview of what it's like to develop on Android -->
  <!--<li style="color:red">Overview</li> -->

  <li class="nav-section">
    <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/components/index.html">
        <span class="en">应用程序组件</span>
      </a></div>
    <ul>
      <li><a href="<?cs var:toroot ?>guide/components/fundamentals.html">
            <span class="en">应用程序基础</span></a>
      </li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/components/activities.html">
            <span class="en">活动</span>
          </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/components/fragments.html">
              <span class="en">片段</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/components/loaders.html">
              <span class="en">载入器</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/components/tasks-and-back-stack.html">
              <span class="en">任务和返回堆栈</span>
            </a></li>
        </ul>
      </li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/components/services.html">
            <span class="en">服务</span>
          </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/components/bound-services.html">
              <span class="en">绑定服务</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/components/aidl.html">
              <span class="en">AIDL</span>
            </a></li>
        </ul>
      </li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/providers/content-providers.html">
            <span class="en">内容提供器</span>
          </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/providers/content-provider-basics.html">
              <span class="en">内容提供器基础</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/providers/content-provider-creating.html">
              <span class="en">创建内容提供器</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/providers/calendar-provider.html">
              <span class="en">日历提供器</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/providers/contacts-provider.html">
              <span class="en">联系人提供器</span>
            </a></li>
        </ul>
      </li>
      <li><a href="<?cs var:toroot ?>guide/components/intents-filters.html">
          <span class="en">意图和意图过滤器</span>
        </a></li>
      <li><a href="<?cs var:toroot ?>guide/components/processes-and-threads.html">
          <span class="en">进程和线程</span>
        </a>
      </li>
      <li><a href="<?cs var:toroot ?>guide/topics/security/permissions.html">
          <span class="en">Permissions</span>
        </a>
      </li>
      <li><a href="<?cs var:toroot ?>guide/topics/appwidgets/index.html">
            <span class="en">App Widgets</span>
          </a></li>
      <li class="nav-section">
      <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/manifest/manifest-intro.html">
          <span class="en">Android Manifest</span>
        </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/action-element.html">&lt;action&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/activity-element.html">&lt;activity&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/activity-alias-element.html">&lt;activity-alias&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/application-element.html">&lt;application&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/category-element.html">&lt;category&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/compatible-screens-element.html">&lt;compatible-screens&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/data-element.html">&lt;data&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/grant-uri-permission-element.html">&lt;grant-uri-permission&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/instrumentation-element.html">&lt;instrumentation&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/intent-filter-element.html">&lt;intent-filter&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/manifest-element.html">&lt;manifest&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/meta-data-element.html">&lt;meta-data&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/path-permission-element.html">&lt;path-permission&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/permission-element.html">&lt;permission&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/permission-group-element.html">&lt;permission-group&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/permission-tree-element.html">&lt;permission-tree&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/provider-element.html">&lt;provider&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/receiver-element.html">&lt;receiver&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/service-element.html">&lt;service&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/supports-gl-texture-element.html">&lt;supports-gl-texture&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/supports-screens-element.html">&lt;supports-screens&gt;</a></li><!-- ##api level 4## -->
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/uses-configuration-element.html">&lt;uses-configuration&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/uses-feature-element.html">&lt;uses-feature&gt;</a></li> <!-- ##api level 4## -->
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/uses-library-element.html">&lt;uses-library&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/uses-permission-element.html">&lt;uses-permission&gt;</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/manifest/uses-sdk-element.html">&lt;uses-sdk&gt;</a></li>
        </ul>
   </li><!-- end of the manifest file -->

    </ul>
  </li>

  <li class="nav-section">
    <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/ui/index.html">
        <span class="en">用户界面</span>
      </a></div>
    <ul>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/overview.html">
          <span class="en">概述</span>
        </a></li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/ui/declaring-layout.html">
            <span class="en">布局</span>
          </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/layout/linear.html">
              <span class="en">线性布局</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/layout/relative.html">
              <span class="en">相对布局</span>
            </a></li>
       <!--
          <li><a href="<?cs var:toroot ?>guide/topics/ui/layout/grid.html">
              <span class="en">网格布局</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/layout/tabs.html">
              <span class="en">选项卡布局</span>
            </a></li>
       -->
          <li><a href="<?cs var:toroot ?>guide/topics/ui/layout/listview.html">
              <span class="en">列表视图</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/layout/gridview.html">
              <span class="en">网格视图</span>
            </a></li>
        </ul>
      </li>

      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/ui/controls.html">
            <span class="en">输入事件</span>
          </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/button.html">
              <span class="en">按钮</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/text.html">
              <span class="en">文本字段</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/checkbox.html">
              <span class="en">多选框</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/radiobutton.html">
              <span class="en">单选按钮</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/togglebutton.html">
              <span class="en">Toggle Buttons</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/spinner.html">
              <span class="en">Spinners</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/pickers.html">
              <span class="en">Pickers</span>
            </a></li>
<!--
          <li><a href="<?cs var:toroot ?>guide/topics/ui/controls/progress.html">
              <span class="en">Seek and Progress Bars</span>
            </a></li>
-->
        </ul>
      </li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/ui-events.html">
          <span class="en">输入事件</span>
        </a></li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/menus.html">
          <span class="en">菜单</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/actionbar.html">
           <span class="en">Action Bar</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/settings.html">
            <span class="en">设置</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/dialogs.html">
           <span class="en">对话框</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/notifiers/notifications.html">
          <span class="en">通知</span>
        </a></li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/notifiers/toasts.html">
          <span class="en">即时通知</span>
        </a></li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/search/index.html">
            <span class="en">检索</span>
          </a></div>
          <ul>
            <li><a href="<?cs var:toroot ?>guide/topics/search/search-dialog.html">Creating a Search Interface</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/search/adding-recent-query-suggestions.html">Adding Recent Query Suggestions</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/search/adding-custom-suggestions.html">Adding Custom Suggestions</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/search/searchable-config.html">Searchable Configuration</a></li>
          </ul>
      </li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/drag-drop.html">
          <span class="en">拖放</span>
        </a></li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/ui/accessibility/index.html">
            <span class="en">Accessibility</span>
          </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/accessibility/apps.html">
              <span class="en">Making Applications Accessible</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/accessibility/checklist.html">
              <span class="en">Accessibility Developer Checklist</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/ui/accessibility/services.html">
              <span class="en">Building Accessibility Services</span>
            </a></li>
        </ul>
      </li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/themes.html">
          <span class="en">主题和样式</span>
        </a></li>
      <li><a href="<?cs var:toroot ?>guide/topics/ui/custom-components.html">
          <span class="en">自定义组件</span>
        </a></li>
    </ul>
  </li><!-- end of User Interface -->

    <li class="nav-section">
      <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/resources/index.html">
           <span class="en">应用程序资源</span>
         </a></div>
      <ul>
        <li><a href="<?cs var:toroot ?>guide/topics/resources/overview.html">
            <span class="en">概述</span>
          </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/resources/providing-resources.html">
            <span class="en">提供资源</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/resources/accessing-resources.html">
            <span class="en">访问资源</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/resources/runtime-changes.html">
              <span class="en">处理运行时变更</span>
            </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/resources/localization.html">
            <span class="en">本地化</span>
          </a></li>
        <li class="nav-section">
          <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/resources/available-resources.html">
              <span class="en">资源类型</span>
            </a></div>
          <ul>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/animation-resource.html">动画</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/color-list-resource.html">颜色状态列表</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/drawable-resource.html">可绘制对象</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/layout-resource.html">布局</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/menu-resource.html">菜单</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/string-resource.html">字符串</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/style-resource.html">风格</a></li>
            <li><a href="<?cs var:toroot ?>guide/topics/resources/more-resources.html">其他类型</a></li>
          </ul>
        </li><!-- end of resource types -->
      </ul>
    </li><!-- end of app resources -->

   <li class="nav-section">
      <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/graphics/index.html">
          <span class="en">动画与图像</span>
        </a></div>
      <ul>
      <li class="nav-section">
        <li><a href="<?cs var:toroot ?>guide/topics/graphics/overview.html">
            <span class="en">概述</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/graphics/prop-animation.html">
            <span class="en">属性动画</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/graphics/view-animation.html">
            <span class="en">视图动画</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/graphics/drawable-animation.html">
            <span class="en">可绘制资源动画</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/graphics/2d-graphics.html">
            <span class="en">Canvas and Drawables</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/graphics/opengl.html">
            <span class="en">OpenGL</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/graphics/hardware-accel.html">
            <span class="en">Hardware Acceleration</span>
          </a></li>
       </ul>
    </li><!-- end of graphics and animation-->

   <li class="nav-section">
            <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/renderscript/index.html">
              <span class="en">Computation</span>
            </a></div>
            <ul>
              <li><a href="<?cs var:toroot ?>guide/topics/renderscript/compute.html">
                    <span class="en">渲染脚本</span></a>
                  </li>

              <li><a href="<?cs var:toroot ?>guide/topics/renderscript/advanced.html">
                    <span class="en">高级渲染脚本</span></a>
                  </li>
              <li><a href="<?cs var:toroot ?>guide/topics/renderscript/reference.html">
                    <span class="en">Runtime API Reference</span></a>
              </li>
             </ul>
   </li>
      <li class="nav-section">
          <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/media/index.html">
            <span class="en">媒体和相机</span>
          </a></div>
          <ul>
            <li><a href="<?cs var:toroot ?>guide/topics/media/mediaplayer.html">
                  <span class="en">媒体播放</span></a>
                </li>
            <li><a href="<?cs var:toroot ?>guide/appendix/media-formats.html">
                   <span class="en">Supported Media Formats</span></a>
                </li>
            <li><a href="<?cs var:toroot ?>guide/topics/media/audio-capture.html">
                  <span class="en">Audio Capture</span></a>
                </li>
            <li><a href="<?cs var:toroot ?>guide/topics/media/jetplayer.html">
                  <span class="en">JetPlayer</span></a>
                </li>
            <li><a href="<?cs var:toroot ?>guide/topics/media/camera.html">
                  <span class="en">相机</span></a>
                </li>
          </ul>
      </li><!-- end of media and camera -->

      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/sensors/index.html">
              <span class="en">Location and Sensors</span>
              </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/location/index.html">
               <span class="en">位置和地图</span>
             </a>
          <li><a href="<?cs var:toroot ?>guide/topics/location/strategies.html">
               <span class="en">Location Strategies</span>
             </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/sensors/sensors_overview.html">
              <span class="en">Sensors Overview</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/sensors/sensors_motion.html">
              <span class="en">Motion Sensors</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/sensors/sensors_position.html">
              <span class="en">Position Sensors</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/sensors/sensors_environment.html">
              <span class="en">Environment Sensors</span>
            </a></li>
        </ul>
      </li><!-- end of location and sensors -->



      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/connectivity/index.html">
               <span class="en">Connectivity</span>
             </a></div>
        <ul>
          <li><a href="<?cs var:toroot?>guide/topics/connectivity/bluetooth.html">
              <span class="en">蓝牙</span>
            </a>
          </li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot?>guide/topics/connectivity/nfc/index.html">
          <span class="en">NFC</span></a>
        </div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/connectivity/nfc/nfc.html">NFC Basics</a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/connectivity/nfc/advanced-nfc.html">Advanced NFC</a></li>
        </ul>
      </li>
      <li><a href="<?cs var:toroot?>guide/topics/connectivity/wifip2p.html">
            <span class="en">Wi-Fi Direct</span></a>
          </li>
      <li class="nav-section">
          <div class="nav-section-header"><a href="<?cs var:toroot?>guide/topics/connectivity/usb/index.html">
            <span class="en">USB</span></a>
          </div>
            <ul>
              <li><a href="<?cs var:toroot ?>guide/topics/connectivity/usb/accessory.html">Accessory</a></li>
              <li><a href="<?cs var:toroot ?>guide/topics/connectivity/usb/host.html">Host</a></li>
            </ul>
     </li>
     <li><a href="<?cs var:toroot?>guide/topics/connectivity/sip.html">
            <span class="en">SIP</span>
          </a>
     </li>

    </ul>
  </li><!-- end of connectivity -->

      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/text/index.html">
            <span class="en">Text and Input</span>
        </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/topics/text/copy-paste.html">
              <span class="en">拷贝和粘贴</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/text/creating-input-method.html">
              <span class="en">创建IME</span>
            </a></li>
          <li><a href="<?cs var:toroot ?>guide/topics/text/spell-checker-framework.html">
              <span class="en">Spelling Checker</span>
            </a></li>
        </ul>
      </li><!-- end of text and input -->

     <li class="nav-section">
      <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/topics/data/index.html">
          <span class="en">Data Storage</span>
        </a></div>
      <ul>
         <li><a href="<?cs var:toroot ?>guide/topics/data/data-storage.html">
            <span class="en">Storage Options</span>
           </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/data/backup.html">
            <span class="en">Data Backup</span>
          </a></li>
        <li><a href="<?cs var:toroot ?>guide/topics/data/install-location.html">
            <span class="en">App Install Location</span>
          </a></li>
      </ul>
    </li><!-- end of data storage -->


  <li class="nav-section">
           <div class="nav-section-header"><a href="<?cs var:toroot?>guide/topics/admin/index.html">
               <span class="en">管理</span>
             </a></div>
           <ul>
              <li>
                <a href="<?cs var:toroot?>guide/topics/admin/device-admin.html">
                <span class="en">Device Policies</span></a>
              </li>
            <!--
              <li>
                <a href="<?cs var:toroot?>guide/topics/admin/keychain.html">
                <span class="en">Certificate Store</span></a>
              </li>
            -->
           </ul>
  </li><!-- end of administration -->

  <li class="nav-section">
    <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/webapps/index.html">
    <span class="en">Web Apps</span>
    </a></div>
    <ul>
      <li><a href="<?cs var:toroot ?>guide/webapps/overview.html">
            <span class="en">概述</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/webapps/targeting.html">
            <span class="en">Targeting Screens from Web Apps</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/webapps/webview.html">
            <span class="en">Building Web Apps in WebView</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/webapps/debugging.html">
            <span class="en">Debugging Web Apps</span>
          </a></li>
      <li><a href="<?cs var:toroot ?>guide/webapps/best-practices.html">
            <span class="en">Best Practices for Web Apps</span>
          </a></li>
    </ul>
  </li><!-- end of web apps -->

  <li class="nav-section">
    <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/practices/index.html">
      <span class="en">Best Practices</span>
      <span class="de" style="display:none">Bewährte Verfahren</span>
      <span class="es" style="display:none">Prácticas recomendadas</span>
      <span class="fr" style="display:none">Meilleures pratiques</span>
      <span class="it" style="display:none">Best practice</span>
      <span class="ja" style="display:none">ベスト プラクティス</span>
      <span class="zh-CN" style="display:none">最佳实践</span>
      <span class="zh-TW" style="display:none">最佳實務</span>
    </div></a>
    <ul>
      <li><a href="<?cs var:toroot ?>guide/practices/compatibility.html">
            <span class="en">Compatibility</span>
          </a></li>
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/practices/screens_support.html">
          <span class="en">Supporting Multiple Screens</span>
        </a></div>
        <ul>
          <li><a href="<?cs var:toroot ?>guide/practices/screens-distribution.html">
            <span class="en">Distributing to Specific Screens</span>
          </a></li>
          <li><a href="<?cs var:toroot ?>guide/practices/screen-compat-mode.html">
            <span class="en">Screen Compatibility Mode</span>
          </a></li>
          <!--<li><a href="<?cs var:toroot ?>guide/practices/screens-support-1.5.html">
            <span class="en">Strategies for Android 1.5</span>
          </a></li> -->
        </ul>
      </li>
      <li><a href="<?cs var:toroot ?>guide/practices/tablets-and-handsets.html">
            <span class="en">Supporting Tablets and Handsets</span>
          </a></li>

    </ul>
  </li>

      <!-- this needs to move
      <li class="nav-section">
        <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/index.html">
               <span class="en">UI Guidelines</span>
             </a></div>
        <ul>
          <li class="nav-section">
            <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design.html">
                   <span class="en">Icon Design</span>
                 </a></div>
            <ul>
              <li><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design_launcher.html">
                    <span class="en">Launcher Icons</span>
                  </a></li>
              <li><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design_menu.html">
                    <span class="en">Menu Icons</span>
                  </a></li>
              <li><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design_action_bar.html">
                    <span class="en">Action Bar Icons</span>
                  </a></li>
              <li><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design_status_bar.html">
                    <span class="en">Status Bar Icons</span>
                  </a></li>
              <li><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design_tab.html">
                    <span class="en">Tab Icons</span>
                  </a></li>
              <li><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design_dialog.html">
                    <span class="en">Dialog Icons</span>
                  </a></li>
              <li><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/icon_design_list.html">
                    <span class="en">List View Icons</span>
                  </a></li>
            </ul>
          </li>
          <li><div><a href="<?cs var:toroot ?>guide/practices/ui_guidelines/widget_design.html">
                <span class="en">App Widget Design</span>
              </a></div>
          </li>
        </ul>
      </li>
        </ul> -->

<!-- Remove
  <li class="nav-section">
    <div class="nav-section-header"><a href="<?cs var:toroot ?>guide/appendix/index.html">
        <span class="en">Appendix</span>
        <span class="de" style="display:none">Anhang</span>
        <span class="es" style="display:none">Apéndice</span>
        <span class="fr" style="display:none">Annexes</span>
        <span class="it" style="display:none">Appendice</span>
        <span class="ja" style="display:none">付録</span>
        <span class="zh-CN" style="display:none">附录</span>
        <span class="zh-TW" style="display:none">附錄</span>
      </a></div>
    <ul>
      <li><a href="<?cs var:toroot ?>guide/appendix/g-app-intents.html">
            <span class="en">Intents List: Google Apps</span>
          </a></li>


      <li><a href="<?cs var:toroot ?>guide/appendix/glossary.html">
            <span class="en">Glossary</span>
          </a></li>
    </ul>
  </li>

</li>
-->
</ul>


<script type="text/javascript">
<!--
    buildToggleLists();
    changeNavLang(getLangPref());
//-->
</script>

